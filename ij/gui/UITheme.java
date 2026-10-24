package ij.gui;

import java.awt.Color;
import java.awt.Font;

/** Shared colors and fonts for the ImageX application shell. */
public final class UITheme {

	public static final Color BACKGROUND = new Color(245, 246, 248);
	public static final Color CARD = new Color(255, 255, 255);
	public static final Color CANVAS = new Color(232, 234, 237);
	public static final Color STATUS = new Color(250, 251, 252);
	public static final Color TEXT = new Color(31, 35, 40);
	public static final Color MUTED = new Color(102, 112, 122);
	public static final Color ACCENT = new Color(0, 122, 255);
	public static final Color ACCENT_HOVER = new Color(0, 102, 220);
	public static final Color BORDER = new Color(218, 222, 228);

	private UITheme() {
	}

	public static Font font(int style, int size) {
		String os = System.getProperty("os.name", "").toLowerCase();
		String name = os.indexOf("windows")>=0 ? "Microsoft YaHei UI" : "SansSerif";
		return new Font(name, style, size);
	}
}
