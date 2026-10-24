package ij.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.filechooser.FileSystemView;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Method;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.I18n;

/** Shared colors and fonts for the FocalScope application shell. */
public final class UITheme {

	private static final String[] SUPPORTED_IMAGE_EXTENSIONS = {
		"tif", "tiff", "png", "jpg", "jpeg", "jpe", "jfif", "gif",
		"bmp", "dib", "dcm", "dicom", "ima", "fits", "fit", "fts",
		"fits.gz", "fts.gz", "pgm", "ppm", "pbm", "pnm"
	};

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
		configureFileList(chooser);
	}

	public static void setSupportedImageFilter(JFileChooser chooser) {
		if (chooser==null) return;
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("支持的图像文件",
			SUPPORTED_IMAGE_EXTENSIONS));
	}

	private static JList<?> findList(Container container) {
		for (Component child : container.getComponents()) {
			if (child instanceof JList)
				return (JList<?>)child;
			if (child instanceof Container) {
				JList<?> list = findList((Container)child);
				if (list!=null) return list;
			}
		}
		return null;
	}

	private static void configureFileList(JFileChooser chooser) {
		final JList<?> list = findList(chooser);
		if (list==null) return;
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		list.setFixedCellWidth(FileGridCellRenderer.CELL_WIDTH);
		list.setFixedCellHeight(FileGridCellRenderer.CELL_HEIGHT);
		final FileGridCellRenderer renderer = new FileGridCellRenderer(list);
		list.setCellRenderer(renderer);
		final int[] hoverIndex = {-1};
		list.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent event) {
				int index = list.locationToIndex(event.getPoint());
				Rectangle bounds = index>=0 ? list.getCellBounds(index, index) : null;
				if (bounds==null || !bounds.contains(event.getPoint())) index = -1;
				if (index!=hoverIndex[0]) {
					hoverIndex[0] = index;
					renderer.setHoverIndex(index);
					list.repaint();
				}
			}
		});
		list.addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent event) {
				hoverIndex[0] = -1;
				renderer.setHoverIndex(-1);
				list.repaint();
			}
		});
	}

	private static Icon systemIcon(FileSystemView view, File file, int size) {
		try {
			Method method = FileSystemView.class.getMethod("getSystemIcon", File.class, int.class, int.class);
			return (Icon)method.invoke(view, file, size, size);
		} catch (Exception e) {
			Icon icon = view.getSystemIcon(file);
			return icon!=null ? scaleIcon(icon, size) : null;
		}
	}

	private static Icon scaleIcon(Icon icon, int size) {
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		if (width<=0 || height<=0) return icon;
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.scale((double)size/width, (double)size/height);
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		return new ImageIcon(image);
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
