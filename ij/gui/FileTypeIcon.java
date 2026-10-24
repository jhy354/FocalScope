package ij.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.Icon;

/** Crisp, flat file/folder icons drawn as vectors, or image thumbnails. */
public class FileTypeIcon implements Icon {

	private static final int SIZE = 40;
	private static final Color IMAGE_BORDER = new Color(205, 212, 220);

	private final File file;
	private final Color accent;
	private final BufferedImage thumbnail;

	public FileTypeIcon(File file) {
		this(file, null);
	}

	public FileTypeIcon(File file, Runnable onThumbnailLoaded) {
		this.file = file;
		this.accent = accentFor(file);
		BufferedImage image = null;
		if (file!=null && !file.isDirectory() && isImage(extension(file.getName().toLowerCase()))) {
			image = FileThumbnailLoader.get(file);
			if (image==null)
				FileThumbnailLoader.request(file, onThumbnailLoaded);
		}
		this.thumbnail = image;
	}

	public int getIconWidth() {
		return SIZE;
	}

	public int getIconHeight() {
		return SIZE;
	}

	public void paintIcon(Component component, Graphics graphics, int x, int y) {
		Graphics2D g = (Graphics2D)graphics.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.translate(x, y);
		if (file!=null && file.isDirectory())
			paintFolder(g);
		else if (thumbnail!=null)
			paintThumbnail(g);
		else
			paintFile(g);
		g.dispose();
	}

	private void paintThumbnail(Graphics2D g) {
		Shape oldClip = g.getClip();
		g.setColor(Color.WHITE);
		g.fillRoundRect(2, 2, 36, 36, 7, 7);
		g.setClip(new RoundRectangle2D.Float(2, 2, 36, 36, 7, 7));
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(thumbnail, 2, 2, 36, 36, null);
		g.setClip(oldClip);
		g.setColor(IMAGE_BORDER);
		g.setStroke(new BasicStroke(1f));
		g.drawRoundRect(2, 2, 35, 35, 7, 7);
	}

	private void paintFolder(Graphics2D g) {
		g.setColor(new Color(255, 199, 82));
		g.fill(new RoundRectangle2D.Float(3, 9, 34, 27, 7, 7));
		g.setColor(new Color(255, 214, 120));
		g.fill(new RoundRectangle2D.Float(4, 13, 32, 20, 5, 5));
		g.setColor(new Color(224, 164, 48));
		g.setStroke(new BasicStroke(1.2f));
		g.draw(new RoundRectangle2D.Float(3.5f, 9.5f, 33, 26, 7, 7));
		g.setColor(new Color(245, 178, 55));
		g.fill(new RoundRectangle2D.Float(4, 6, 15, 8, 4, 4));
	}

	private void paintFile(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fill(new RoundRectangle2D.Float(7, 3, 26, 34, 4, 4));
		g.setColor(new Color(207, 213, 220));
		g.setStroke(new BasicStroke(1.2f));
		g.draw(new RoundRectangle2D.Float(7.5f, 3.5f, 25, 33, 4, 4));

		Path2D.Float fold = new Path2D.Float();
		fold.moveTo(25, 3);
		fold.lineTo(33, 11);
		fold.lineTo(25, 11);
		fold.closePath();
		g.setColor(new Color(235, 239, 243));
		g.fill(fold);
		g.setColor(new Color(207, 213, 220));
		g.draw(fold);

		String name = file!=null ? file.getName().toLowerCase() : "";
		String ext = extension(name);
		g.setColor(accent);
		if (isImage(ext)) {
			g.fill(new RoundRectangle2D.Float(11, 15, 18, 13, 2, 2));
			g.setColor(Color.WHITE);
			g.fillOval(14, 17, 4, 4);
			Path2D.Float mountain = new Path2D.Float();
			mountain.moveTo(12, 26);
			mountain.lineTo(18, 20);
			mountain.lineTo(22, 24);
			mountain.lineTo(25, 21);
			mountain.lineTo(28, 26);
			mountain.closePath();
			g.fill(mountain);
		} else if (isText(ext)) {
			g.setColor(new Color(116, 126, 137));
			for (int i=0; i<4; i++)
				g.fillRoundRect(12, 13+i*5, i==3?11:17, 2, 2, 2);
		} else if (isArchive(ext)) {
			g.setColor(new Color(120, 144, 170));
			g.fillRoundRect(12, 12, 16, 4, 2, 2);
			g.fillRoundRect(12, 19, 16, 4, 2, 2);
			g.fillRoundRect(12, 26, 16, 4, 2, 2);
		} else {
			g.setColor(accent);
			g.fillRoundRect(12, 12, 9, 3, 2, 2);
			g.setColor(new Color(190, 198, 207));
			for (int i=0; i<3; i++)
				g.fillRoundRect(12, 18+i*5, i==2?10:16, 2, 2, 2);
		}
	}

	private static String extension(String name) {
		int index = name.lastIndexOf('.');
		return index>=0 ? name.substring(index+1) : "";
	}

	private static boolean isImage(String ext) {
		return "tif".equals(ext) || "tiff".equals(ext) || "png".equals(ext) || "jpg".equals(ext)
			|| "jpeg".equals(ext) || "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext)
			|| "dcm".equals(ext) || "dicom".equals(ext) || "fits".equals(ext)
			|| "fit".equals(ext) || "pgm".equals(ext);
	}

	private static boolean isText(String ext) {
		return "txt".equals(ext) || "csv".equals(ext) || "tsv".equals(ext) || "md".equals(ext)
			|| "json".equals(ext) || "xml".equals(ext) || "java".equals(ext) || "js".equals(ext)
			|| "py".equals(ext) || "ijm".equals(ext);
	}

	private static boolean isArchive(String ext) {
		return "zip".equals(ext) || "gz".equals(ext) || "tar".equals(ext) || "7z".equals(ext)
			|| "rar".equals(ext) || "bz2".equals(ext);
	}

	private static Color accentFor(File file) {
		if (file==null) return new Color(120, 144, 170);
		String ext = extension(file.getName().toLowerCase());
		if (isImage(ext)) return new Color(0, 122, 255);
		if (isText(ext)) return new Color(90, 160, 120);
		if (isArchive(ext)) return new Color(130, 115, 190);
		return new Color(120, 144, 170);
	}
}
