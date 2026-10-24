package ij.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import ij.ImagePlus;
import ij.io.Opener;

/** Loads small, high-resolution thumbnails for the file chooser grid. */
public final class FileThumbnailLoader {

	/** Thumbnails are rendered at 2x so they stay sharp on high-DPI displays. */
	private static final int THUMBNAIL_SIZE = 96;

	private static final Map<String, SoftReference<BufferedImage>> CACHE =
		new ConcurrentHashMap<String, SoftReference<BufferedImage>>();
	private static final Set<String> LOADING =
		Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private static final Set<String> MISSING =
		Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2,
		new ThreadFactory() {
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "FocalScope thumbnail loader");
				thread.setDaemon(true);
				thread.setPriority(Thread.MIN_PRIORITY);
				return thread;
			}
		});

	private FileThumbnailLoader() {
	}

	public static BufferedImage get(File file) {
		if (file==null) return null;
		SoftReference<BufferedImage> reference = CACHE.get(key(file));
		return reference!=null ? reference.get() : null;
	}

	/**
	 * Loads a thumbnail in the background. The callback is always invoked on the
	 * event dispatch thread, even when the file cannot be decoded.
	 */
	public static void request(final File file, final Runnable onLoaded) {
		if (file==null) return;
		final String key = key(file);
		SoftReference<BufferedImage> reference = CACHE.get(key);
		if (reference!=null && reference.get()!=null) {
			return;
		}
		if (MISSING.contains(key)) {
			return;
		}
		if (!LOADING.add(key)) return;
		EXECUTOR.execute(new Runnable() {
			public void run() {
				BufferedImage image = null;
				try {
					image = load(file);
				} catch (Throwable t) {
					image = null;
				}
				LOADING.remove(key);
				if (image!=null)
					CACHE.put(key, new SoftReference<BufferedImage>(image));
				else
					MISSING.add(key);
				invokeLater(onLoaded);
			}
		});
	}

	private static void invokeLater(Runnable runnable) {
		if (runnable==null) return;
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}

	private static String key(File file) {
		return file.getAbsolutePath() + "\u0000" + file.lastModified() + "\u0000" + file.length();
	}

	private static BufferedImage load(File file) throws Exception {
		BufferedImage source = null;
		try {
			source = ImageIO.read(file);
		} catch (Exception e) {
			source = null;
		}
		if (source!=null) {
			try {
				return makeThumbnail(source);
			} catch (RuntimeException e) {
				// Some 32-bit TIFFs cannot be blitted by Java2D. Try ImageJ below.
				return makeImageJThumbnail(file);
			}
		}
		if (isImageJOnlyFormat(file.getName().toLowerCase()))
			return makeImageJThumbnail(file);
		return null;
	}

	private static boolean isImageJOnlyFormat(String name) {
		String ext = extension(name);
		return "dcm".equals(ext) || "dicom".equals(ext) || "fits".equals(ext)
			|| "fit".equals(ext) || "pgm".equals(ext);
	}

	private static BufferedImage makeImageJThumbnail(File file) {
		if (!isImageJFormat(file.getName().toLowerCase())) return null;
		Opener opener = new Opener();
		opener.setSilentMode(true);
		ImagePlus image = opener.openImage(file.getAbsolutePath());
		if (image==null || image.getWidth()<=0 || image.getHeight()<=0) return null;
		BufferedImage source = image.getBufferedImage();
		if (source==null) return null;
		return makeThumbnail(source);
	}

	private static boolean isImageJFormat(String name) {
		String ext = extension(name);
		return "tif".equals(ext) || "tiff".equals(ext) || "png".equals(ext) || "jpg".equals(ext)
			|| "jpeg".equals(ext) || "gif".equals(ext) || "bmp".equals(ext)
			|| "dcm".equals(ext) || "dicom".equals(ext) || "fits".equals(ext)
			|| "fit".equals(ext) || "pgm".equals(ext);
	}

	private static String extension(String name) {
		int index = name.lastIndexOf('.');
		return index>=0 ? name.substring(index+1) : "";
	}

	private static BufferedImage makeThumbnail(BufferedImage source) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		if (sourceWidth<=0 || sourceHeight<=0) return null;

		double scale = Math.min((double)THUMBNAIL_SIZE/sourceWidth,
			(double)THUMBNAIL_SIZE/sourceHeight);
		int width = Math.max(1, (int)Math.round(sourceWidth*scale));
		int height = Math.max(1, (int)Math.round(sourceHeight*scale));

		BufferedImage thumbnail = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE,
			BufferedImage.TYPE_INT_RGB);
		Graphics2D g = thumbnail.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(source, (THUMBNAIL_SIZE-width)/2, (THUMBNAIL_SIZE-height)/2,
			width, height, null);
		g.dispose();
		return thumbnail;
	}
}
