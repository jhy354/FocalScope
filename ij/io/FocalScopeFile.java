package ij.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;

/**
 * FocalScope container format (.fsz): a ZIP file holding a TIFF image,
 * calibration/properties, the current ROI, overlay ROIs and ROI Manager ROIs.
 */
public final class FocalScopeFile {

	public static final String EXTENSION = ".fsz";
	public static final String PATH_KEY = "focalscope.path";
	private static final String FORMAT = "FocalScope";
	private static final String VERSION = "1";

	private FocalScopeFile() {
	}

	/** Saves to the current .fsz path, or shows a Save dialog. */
	public static boolean save(ImagePlus imp) {
		if (imp==null) return false;
		Object value = imp.getProperty(PATH_KEY);
		String path = value instanceof String ? (String)value : null;
		if (path==null || path.length()==0) {
			FileInfo fi = imp.getOriginalFileInfo();
			if (fi!=null && fi.fileName!=null && fi.fileName.toLowerCase().endsWith(EXTENSION))
				path = (fi.directory!=null?fi.directory:"") + fi.fileName;
		}
		if (path==null || path.length()==0)
			return saveAs(imp);
		return save(imp, path);
	}

	/** Always shows a Save dialog for a .fsz file. */
	public static boolean saveAs(ImagePlus imp) {
		if (imp==null) return false;
		String name = imp.getTitle();
		if (name==null || name.length()==0)
			name = "Untitled";
		int dot = name.lastIndexOf('.');
		if (dot>0)
			name = name.substring(0, dot);
		SaveDialog sd = new SaveDialog("Save As...", name, EXTENSION);
		String dir = sd.getDirectory();
		String file = sd.getFileName();
		if (dir==null || file==null)
			return false;
		return save(imp, dir+file);
	}

	/** Saves an image and its associated data to the specified .fsz path. */
	public static boolean save(ImagePlus imp, String path) {
		if (imp==null || path==null) return false;
		if (!path.toLowerCase().endsWith(EXTENSION))
			path += EXTENSION;
		File temp = null;
		ZipOutputStream zip = null;
		try {
			temp = File.createTempFile("focalscope-", ".tif");
			FileSaver saver = new FileSaver(imp);
			if (!saver.saveAsTiff(temp.getPath()))
				return false;

			zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			copyToZip(temp, zip, "image.tif");

			Properties manifest = new Properties();
			manifest.setProperty("format", FORMAT);
			manifest.setProperty("version", VERSION);
			manifest.setProperty("title", imp.getTitle()!=null?imp.getTitle():"");
			manifest.setProperty("width", Integer.toString(imp.getWidth()));
			manifest.setProperty("height", Integer.toString(imp.getHeight()));
			manifest.setProperty("stackSize", Integer.toString(imp.getStackSize()));
			saveCalibration(manifest, imp.getCalibration());

			Roi roi = imp.getRoi();
			if (roi!=null) {
				manifest.setProperty("roi.count", "1");
				writeRoi(zip, "roi.roi", roi);
			}

			Overlay overlay = imp.getOverlay();
			if (overlay!=null && overlay.size()>0) {
				manifest.setProperty("overlay.count", Integer.toString(overlay.size()));
				for (int i=0; i<overlay.size(); i++)
					writeRoi(zip, "overlay/"+pad(i)+".roi", overlay.get(i));
			}

			RoiManager rm = RoiManager.getInstance();
			if (rm!=null && rm.getCount()>0) {
				manifest.setProperty("roiManager.count", Integer.toString(rm.getCount()));
				Roi[] rois = rm.getRoisAsArray();
				for (int i=0; i<rois.length; i++)
					writeRoi(zip, "roi-manager/"+pad(i)+".roi", rois[i]);
			}

			String[] props = imp.getPropertiesAsArray();
			if (props!=null && props.length>0) {
				Properties imageProps = new Properties();
				for (int i=0; i+1<props.length; i+=2)
					imageProps.setProperty(props[i], props[i+1]!=null?props[i+1]:"");
				putProperties(zip, "properties.properties", imageProps);
			}

			putProperties(zip, "manifest.properties", manifest);
			zip.close();
			zip = null;
			imp.setProperty(PATH_KEY, path);
			imp.changes = false;
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (zip!=null) {
				try { zip.close(); } catch (Exception e) {}
			}
			if (temp!=null) temp.delete();
		}
	}

	/** Opens a .fsz file and restores image, calibration, ROI, overlay and ROI Manager data. */
	public static ImagePlus open(String path) {
		ZipFile zip = null;
		File temp = null;
		try {
			zip = new ZipFile(path);
			Properties manifest = loadProperties(zip, "manifest.properties");
			if (manifest==null || !FORMAT.equals(manifest.getProperty("format")))
				return null;
			ZipEntry imageEntry = zip.getEntry("image.tif");
			if (imageEntry==null)
				return null;
			temp = File.createTempFile("focalscope-open-", ".tif");
			copyFromZip(zip, imageEntry, temp);
			ImagePlus imp = new Opener().openImage(temp.getPath());
			if (imp==null)
				return null;

			String title = manifest.getProperty("title");
			if (title!=null && title.length()>0)
				imp.setTitle(title);
			applyCalibration(manifest, imp);
			Properties props = loadProperties(zip, "properties.properties");
			if (props!=null && props.size()>0) {
				String[] array = new String[props.size()*2];
				int n = 0;
				Enumeration en = props.keys();
				while (en.hasMoreElements()) {
					String key = (String)en.nextElement();
					array[n++] = key;
					array[n++] = props.getProperty(key);
				}
				imp.setProperties(array);
			}

			int overlayCount = getInt(manifest, "overlay.count");
			Overlay overlay = null;
			for (int i=0; i<overlayCount; i++) {
				Roi roi = readRoi(zip, "overlay/"+pad(i)+".roi");
				if (roi!=null) {
					if (overlay==null) overlay = new Overlay();
					overlay.add(roi);
				}
			}
			if (overlay!=null)
				imp.setOverlay(overlay);

			int managerCount = getInt(manifest, "roiManager.count");
			if (managerCount>0) {
				RoiManager rm = RoiManager.getInstance();
				if (rm==null)
					rm = RoiManager.getRoiManager();
				for (int i=0; i<managerCount; i++) {
					Roi roi = readRoi(zip, "roi-manager/"+pad(i)+".roi");
					if (roi!=null)
						rm.addRoi(roi);
				}
			}

			Roi roi = readRoi(zip, "roi.roi");
			if (roi!=null)
				imp.setRoi(roi);
			imp.setProperty(PATH_KEY, path);
			imp.changes = false;
			return imp;
		} catch (Exception e) {
			return null;
		} finally {
			if (zip!=null) {
				try { zip.close(); } catch (Exception e) {}
			}
			if (temp!=null)
				temp.deleteOnExit();
		}
	}

	private static void saveCalibration(Properties p, Calibration cal) {
		if (cal==null) return;
		p.setProperty("cal.unit", cal.getUnit());
		p.setProperty("cal.xUnit", cal.getXUnit());
		p.setProperty("cal.yUnit", cal.getYUnit());
		p.setProperty("cal.zUnit", cal.getZUnit());
		p.setProperty("cal.valueUnit", cal.getValueUnit());
		p.setProperty("cal.timeUnit", cal.getTimeUnit());
		p.setProperty("cal.pixelWidth", Double.toString(cal.pixelWidth));
		p.setProperty("cal.pixelHeight", Double.toString(cal.pixelHeight));
		p.setProperty("cal.pixelDepth", Double.toString(cal.pixelDepth));
		p.setProperty("cal.frameInterval", Double.toString(cal.frameInterval));
		p.setProperty("cal.xOrigin", Double.toString(cal.xOrigin));
		p.setProperty("cal.yOrigin", Double.toString(cal.yOrigin));
		p.setProperty("cal.zOrigin", Double.toString(cal.zOrigin));
		p.setProperty("cal.function", Integer.toString(cal.getFunction()));
		double[] coeff = cal.getCoefficients();
		if (coeff!=null) {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<coeff.length; i++) {
				if (i>0) sb.append(',');
				sb.append(coeff[i]);
			}
			p.setProperty("cal.coefficients", sb.toString());
		}
	}

	private static void applyCalibration(Properties p, ImagePlus imp) {
		if (p==null) return;
		Calibration cal = new Calibration();
		cal.setUnit(p.getProperty("cal.unit", "pixel"));
		cal.setXUnit(p.getProperty("cal.xUnit", ""));
		cal.setYUnit(p.getProperty("cal.yUnit", ""));
		cal.setZUnit(p.getProperty("cal.zUnit", ""));
		cal.setValueUnit(p.getProperty("cal.valueUnit", Calibration.DEFAULT_VALUE_UNIT));
		cal.setTimeUnit(p.getProperty("cal.timeUnit", "sec"));
		cal.pixelWidth = getDouble(p, "cal.pixelWidth", 1.0);
		cal.pixelHeight = getDouble(p, "cal.pixelHeight", 1.0);
		cal.pixelDepth = getDouble(p, "cal.pixelDepth", 1.0);
		cal.frameInterval = getDouble(p, "cal.frameInterval", 0.0);
		cal.xOrigin = getDouble(p, "cal.xOrigin", 0.0);
		cal.yOrigin = getDouble(p, "cal.yOrigin", 0.0);
		cal.zOrigin = getDouble(p, "cal.zOrigin", 0.0);
		int function = getInt(p, "cal.function");
		double[] coeff = parseCoefficients(p.getProperty("cal.coefficients"));
		if (function!=Calibration.NONE && coeff!=null)
			cal.setFunction(function, coeff, p.getProperty("cal.valueUnit", Calibration.DEFAULT_VALUE_UNIT));
		imp.setCalibration(cal);
	}

	private static void writeRoi(ZipOutputStream zip, String name, Roi roi) throws IOException {
		if (roi==null) return;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RoiEncoder encoder = new RoiEncoder(out);
		encoder.write(roi);
		byte[] bytes = out.toByteArray();
		zip.putNextEntry(new ZipEntry(name));
		zip.write(bytes);
		zip.closeEntry();
	}

	private static Roi readRoi(ZipFile zip, String name) {
		try {
			ZipEntry entry = zip.getEntry(name);
			if (entry==null) return null;
			byte[] bytes = readEntry(zip, entry);
			return new RoiDecoder(bytes, name).getRoi();
		} catch (Exception e) {
			return null;
		}
	}

	private static void putProperties(ZipOutputStream zip, String name, Properties props) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		props.store(zip, "FocalScope");
		zip.closeEntry();
	}

	private static Properties loadProperties(ZipFile zip, String name) {
		try {
			ZipEntry entry = zip.getEntry(name);
			if (entry==null) return null;
			Properties props = new Properties();
			InputStream in = zip.getInputStream(entry);
			try { props.load(in); } finally { in.close(); }
			return props;
		} catch (Exception e) {
			return null;
		}
	}

	private static void copyToZip(File file, ZipOutputStream zip, String name) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			byte[] buffer = new byte[65536];
			int n;
			while ((n=in.read(buffer))>0)
				zip.write(buffer, 0, n);
		} finally { in.close(); }
		zip.closeEntry();
	}

	private static void copyFromZip(ZipFile zip, ZipEntry entry, File file) throws IOException {
		InputStream in = new BufferedInputStream(zip.getInputStream(entry));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			byte[] buffer = new byte[65536];
			int n;
			while ((n=in.read(buffer))>0)
				out.write(buffer, 0, n);
		} finally {
			in.close();
			out.close();
		}
	}

	private static byte[] readEntry(ZipFile zip, ZipEntry entry) throws IOException {
		InputStream in = new BufferedInputStream(zip.getInputStream(entry));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[8192];
			int n;
			while ((n=in.read(buffer))>0)
				out.write(buffer, 0, n);
		} finally { in.close(); }
		return out.toByteArray();
	}

	private static int getInt(Properties p, String key) {
		return getInt(p, key, 0);
	}

	private static int getInt(Properties p, String key, int defaultValue) {
		try { return Integer.parseInt(p.getProperty(key)); }
		catch (Exception e) { return defaultValue; }
	}

	private static double getDouble(Properties p, String key, double defaultValue) {
		try { return Double.parseDouble(p.getProperty(key)); }
		catch (Exception e) { return defaultValue; }
	}

	private static double[] parseCoefficients(String s) {
		if (s==null || s.length()==0) return null;
		String[] parts = s.split(",");
		double[] values = new double[parts.length];
		try {
			for (int i=0; i<parts.length; i++)
				values[i] = Double.parseDouble(parts[i]);
			return values;
		} catch (Exception e) {
			return null;
		}
	}

	private static String pad(int n) {
		if (n<10) return "00"+n;
		if (n<100) return "0"+n;
		return Integer.toString(n);
	}
}
