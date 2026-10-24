package ij.plugin;

import ij.IJ;
import ij.Prefs;
import ij.gui.ImageCanvas;

/** Toggles the FocalScope mouse navigation options. */
public class FocalScopeViewOptions implements PlugIn {

	public void run(String arg) {
		if ("wheel".equals(arg)) {
			ImageCanvas.wheelZoomEnabled = !ImageCanvas.wheelZoomEnabled;
			Prefs.set("imagex.wheelZoom", ImageCanvas.wheelZoomEnabled);
			IJ.showStatus("FocalScope wheel zoom: " + (ImageCanvas.wheelZoomEnabled ? "on" : "off"));
		}
		else if ("pan".equals(arg)) {
			ImageCanvas.middlePanEnabled = !ImageCanvas.middlePanEnabled;
			Prefs.set("imagex.middlePan", ImageCanvas.middlePanEnabled);
			IJ.showStatus("FocalScope middle-button pan: " + (ImageCanvas.middlePanEnabled ? "on" : "off"));
		}
	}
}
