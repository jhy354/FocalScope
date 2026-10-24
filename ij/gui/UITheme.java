package ij.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
			Class<?> flat = Class.forName("com.formdev.flatlaf.FlatLightLaf");
			flat.getMethod("setup").invoke(null);
		} catch (Exception e) {
			// Keep the current look and feel if FlatLaf is unavailable.
		}
		UIManager.put("Button.background", CARD);
		UIManager.put("Button.foreground", TEXT);
		UIManager.put("Button.gradient", null);
		UIManager.put("Button.select", new Color(226, 238, 255));
		UIManager.put("Panel.background", CARD);
		UIManager.put("control", CARD);
		UIManager.put("Label.foreground", TEXT);
		UIManager.put("List.background", CARD);
		UIManager.put("List.foreground", TEXT);
		UIManager.put("List.selectionBackground", ACCENT);
		UIManager.put("List.selectionForeground", Color.WHITE);
		UIManager.put("Table.background", CARD);
		UIManager.put("Table.foreground", TEXT);
		UIManager.put("Table.selectionBackground", ACCENT);
		UIManager.put("Table.selectionForeground", Color.WHITE);
		UIManager.put("TextField.background", CARD);
		UIManager.put("TextField.foreground", TEXT);
		UIManager.put("ComboBox.background", CARD);
		UIManager.put("ComboBox.foreground", TEXT);
		UIManager.put("FileChooser.openButtonText", "打开");
		UIManager.put("FileChooser.saveButtonText", "保存");
		UIManager.put("FileChooser.cancelButtonText", I18n.label("Cancel"));
		UIManager.put("FileChooser.fileNameLabelText", "文件名");
		UIManager.put("FileChooser.filesOfTypeLabelText", "文件类型");
		UIManager.put("FileChooser.lookInLabelText", "位置");
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
		chooser.setOpaque(true);
		chooser.setPreferredSize(new Dimension(860, 560));
		chooser.setControlButtonsAreShown(true);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setApproveButtonText(save?"保存":"打开");
		chooser.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		styleComponent(chooser);
	}

	private static void styleComponent(Component component) {
		if (component instanceof JButton) {
			JButton button = (JButton)component;
			button.setFont(font(Font.PLAIN, 13));
			button.setBackground(CARD);
			button.setForeground(TEXT);
			button.setFocusPainted(false);
			button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(5, 10, 5, 10)));
		} else if (component instanceof JTextField) {
			JTextField field = (JTextField)component;
			field.setFont(font(Font.PLAIN, 13));
			field.setBackground(CARD);
			field.setForeground(TEXT);
			field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(4, 6, 4, 6)));
		} else if (component instanceof JComboBox) {
			JComboBox<?> combo = (JComboBox<?>)component;
			combo.setFont(font(Font.PLAIN, 13));
			combo.setBackground(CARD);
			combo.setForeground(TEXT);
		} else if (component instanceof JList) {
			JList<?> list = (JList<?>)component;
			list.setFont(font(Font.PLAIN, 13));
			list.setBackground(CARD);
			list.setForeground(TEXT);
		} else if (component instanceof JTable) {
			JTable table = (JTable)component;
			table.setFont(font(Font.PLAIN, 13));
			table.setBackground(CARD);
			table.setForeground(TEXT);
		} else if (component instanceof JLabel) {
			JLabel label = (JLabel)component;
			label.setFont(font(Font.PLAIN, 13));
			label.setForeground(TEXT);
		} else if (component instanceof JPanel) {
			component.setBackground(CARD);
		} else if (component instanceof JScrollPane) {
			component.setBackground(CARD);
			((JScrollPane)component).getViewport().setBackground(CARD);
		}
		if (component instanceof Container) {
			Component[] children = ((Container)component).getComponents();
			for (Component child : children)
				styleComponent(child);
		}
	}
}
