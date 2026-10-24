package ij.plugin;

import ij.IJ;
import ij.Prefs;
import ij.gui.ImageCanvas;

/** Toggles the ImageX mouse navigation options. */
public class ImageXViewOptions implements PlugIn {

	public void run(String arg) {
		if ("wheel".equals(arg)) {
			ImageCanvas.wheelZoomEnabled = !ImageCanvas.wheelZoomEnabled;
			Prefs.set("imagex.wheelZoom", ImageCanvas.wheelZoomEnabled);
			IJ.showStatus("ImageX wheel zoom: " + (ImageCanvas.wheelZoomEnabled ? "on" : "off"));
		}
		else if ("pan".equals(arg)) {
			ImageCanvas.middlePanEnabled = !ImageCanvas.middlePanEnabled;
			Prefs.set("imagex.middlePan", ImageCanvas.middlePanEnabled);
			IJ.showStatus("ImageX middle-button pan: " + (ImageCanvas.middlePanEnabled ? "on" : "off"));
		}
	}
}
