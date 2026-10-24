package ij.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;

/** Labels the length (diameter) of line ROIs stored in the ROI Manager. */
public class ROILabeler implements PlugIn {

	private static final String[] COLOR_NAMES = {
		"黄色", "白色", "红色", "绿色", "青色", "洋红", "黑色", "蓝色"
	};
	private static final Color[] COLORS = {
		Color.yellow, Color.white, Color.red, Color.green,
		Color.cyan, Color.magenta, Color.black, Color.blue
	};
	private static final String[] FONTS = {
		"SansSerif", "Serif", "Monospaced", "Microsoft YaHei UI"
	};

	private static int colorIndex = 0;
	private static int fontIndex = 0;
	private static int fontSize = 14;
	private static boolean bold, italic, underline;

	public void run(String arg) {
		RoiManager rm = RoiManager.getInstance();
		ImagePlus imp = WindowManager.getCurrentImage();
		if (rm==null || rm.getCount()==0 || imp==null) {
			IJ.showStatus("ROI Manager is empty");
			return;
		}

		GenericDialog gd = new GenericDialog("标注 ROI 直径");
		gd.addChoice("颜色:", COLOR_NAMES, COLOR_NAMES[colorIndex]);
		gd.addChoice("字体:", FONTS, FONTS[fontIndex]);
		gd.addNumericField("字号:", fontSize, 0);
		gd.addCheckbox("加粗", bold);
		gd.addCheckbox("斜体", italic);
		gd.addCheckbox("下划线", underline);
		gd.showDialog();
		if (gd.wasCanceled())
			return;

		colorIndex = gd.getNextChoiceIndex();
		fontIndex = gd.getNextChoiceIndex();
		fontSize = (int)Math.round(gd.getNextNumber());
		bold = gd.getNextBoolean();
		italic = gd.getNextBoolean();
		underline = gd.getNextBoolean();
		if (fontSize<6) fontSize = 6;
		if (fontSize>72) fontSize = 72;

		Font font = new Font(FONTS[fontIndex],
			(bold?Font.BOLD:Font.PLAIN)|(italic?Font.ITALIC:Font.PLAIN), fontSize);
		FontMetrics fm = imp.getCanvas()!=null ? imp.getCanvas().getFontMetrics(font) : null;
		if (fm==null)
			fm = new java.awt.Canvas().getFontMetrics(font);
		Calibration cal = imp.getCalibration();
		double pw = cal.pixelWidth>0?cal.pixelWidth:1.0;
		double ph = cal.pixelHeight>0?cal.pixelHeight:1.0;
		String unit = getUnit(cal);

		Overlay overlay = imp.getOverlay();
		if (overlay==null)
			overlay = new Overlay();
		int labeled = 0;
		Roi[] rois = rm.getRoisAsArray();
		for (int i=0; i<rois.length; i++) {
			Roi roi = rois[i];
			int type = roi.getType();
			if (type!=Roi.LINE && type!=Roi.POLYLINE && type!=Roi.FREELINE)
				continue;
			FloatPolygon poly = roi.getFloatPolygon();
			if (poly==null || poly.npoints<2)
				continue;
			double length = 0;
			for (int k=0; k<poly.npoints-1; k++) {
				double dx = (poly.xpoints[k+1]-poly.xpoints[k])*pw;
				double dy = (poly.ypoints[k+1]-poly.ypoints[k])*ph;
				length += Math.sqrt(dx*dx+dy*dy);
			}
			int decimals;
			if (length>=100) decimals = 0;
			else if (length>=10) decimals = 1;
			else if (length>=1) decimals = 2;
			else decimals = 3;
			String text = IJ.d2s(length, decimals) + " " + unit;
			int tw = fm.stringWidth(text);
			java.awt.Rectangle bounds = roi.getBounds();
			int tx = (int)Math.round(bounds.x + bounds.width/2.0 - tw/2.0);
			int ty = (int)Math.round(bounds.y + bounds.height/2.0 + fm.getAscent()/2.0);
			tx = Math.max(2, Math.min(tx, imp.getWidth()-tw-2));
			ty = Math.max(fm.getAscent()+2, Math.min(ty, imp.getHeight()-2));

			TextRoi label = new TextRoi(tx, ty, text, font);
			label.setJustification(TextRoi.LEFT);
			label.setStrokeColor(COLORS[colorIndex]);
			overlay.add(label);
			if (underline) {
				Line line = new Line(tx, ty+2, tx+tw, ty+2);
				line.setStrokeColor(COLORS[colorIndex]);
				line.setStrokeWidth(1);
				overlay.add(line);
			}
			labeled++;
		}
		imp.setOverlay(overlay);
		IJ.showStatus(labeled + " ROI length label(s) added");
	}

	private static String getUnit(Calibration cal) {
		if (cal==null || !cal.scaled())
			return "px";
		String unit = cal.getUnit();
		if (unit==null || unit.length()==0 || "pixel".equals(unit))
			return "px";
		if ("um".equals(unit) || "micron".equals(unit) || "microns".equals(unit))
			return "\u00B5m";
		return unit;
	}
}
