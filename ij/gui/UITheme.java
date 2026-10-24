package ij.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import ij.I18n;

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

	public static void setFileChooserLookAndFeel() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			// Keep the platform look and feel if Nimbus is unavailable.
		}
	}

	public static void styleFileChooser(JFileChooser chooser, String title, boolean save) {
		if (chooser==null) return;
		UIManager.put("FileChooser.openButtonText", "打开");
		UIManager.put("FileChooser.saveButtonText", "保存");
		UIManager.put("FileChooser.cancelButtonText", I18n.label("Cancel"));
		UIManager.put("FileChooser.fileNameLabelText", "文件名");
		UIManager.put("FileChooser.filesOfTypeLabelText", "文件类型");
		UIManager.put("FileChooser.lookInLabelText", "位置");
		chooser.setDialogTitle(I18n.label(title));
		chooser.setFont(font(Font.PLAIN, 13));
		chooser.setBackground(CARD);
		chooser.setForeground(TEXT);
		chooser.setPreferredSize(new Dimension(860, 560));
		chooser.setControlButtonsAreShown(true);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setApproveButtonText(save?"保存":"打开");
	}
}
