package ij.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * Fixed-size icon-grid cell used by the ImageX file chooser. Every cell has the
 * same bounds and inner card, so icons, labels, hover backgrounds and shadows
 * line up regardless of filename length.
 */
public class FileGridCellRenderer implements ListCellRenderer<Object> {

	public static final int CELL_WIDTH = 112;
	public static final int CELL_HEIGHT = 94;

	private static final Color HOVER_FILL = new Color(232, 242, 255);
	private static final Color HOVER_BORDER = new Color(188, 218, 255);
	private static final Color SHADOW = new Color(15, 23, 42, 18);

	private final JList<?> list;
	private final FileCell cell;
	private int hoverIndex = -1;

	public FileGridCellRenderer(JList<?> list) {
		this.list = list;
		this.cell = new FileCell();
	}

	public void setHoverIndex(int index) {
		this.hoverIndex = index;
	}

	public Component getListCellRendererComponent(JList<?> source, Object value,
			int index, boolean selected, boolean focus) {
		cell.configure(value, selected, index==hoverIndex);
		return cell;
	}

	private final class FileCell extends JPanel {

		private final JLabel iconLabel;
		private final JLabel textLabel;
		private boolean selected;
		private boolean hover;

		FileCell() {
			setLayout(null);
			setOpaque(false);
			setBorder(null);
			iconLabel = new JLabel();
			iconLabel.setOpaque(false);
			iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
			iconLabel.setVerticalAlignment(SwingConstants.CENTER);
			textLabel = new JLabel();
			textLabel.setOpaque(false);
			textLabel.setHorizontalAlignment(SwingConstants.CENTER);
			textLabel.setVerticalAlignment(SwingConstants.CENTER);
			textLabel.setFont(UITheme.font(Font.PLAIN, 12));
			add(iconLabel);
			add(textLabel);
		}

		public Dimension getPreferredSize() {
			return new Dimension(CELL_WIDTH, CELL_HEIGHT);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public void doLayout() {
			int width = getWidth();
			int height = getHeight();
			int iconBox = 48;
			int textHeight = 30;
			iconLabel.setBounds((width-iconBox)/2, 8, iconBox, iconBox);
			textLabel.setBounds(6, height-textHeight-6, width-12, textHeight);
		}

		void configure(Object value, boolean isSelected, boolean isHover) {
			this.selected = isSelected;
			this.hover = isHover;
			if (value instanceof File) {
				File file = (File)value;
				String name = file.getName();
				if (name==null || name.length()==0)
					name = file.getAbsolutePath();
				textLabel.setText(ellipsize(name, textLabel.getFontMetrics(textLabel.getFont()), 98));
				textLabel.setForeground(isSelected ? Color.WHITE : UITheme.TEXT);
				textLabel.setToolTipText(file.getAbsolutePath());
				iconLabel.setIcon(new FileTypeIcon(file, new Runnable() {
					public void run() {
						list.repaint();
					}
				}));
				setToolTipText(file.getAbsolutePath());
			} else {
				textLabel.setText(value!=null ? value.toString() : "");
				textLabel.setForeground(isSelected ? Color.WHITE : UITheme.TEXT);
				textLabel.setToolTipText(null);
				iconLabel.setIcon(null);
				setToolTipText(null);
			}
			repaint();
		}

		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D)graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			int x = 4;
			int y = 3;
			int width = Math.max(1, getWidth()-8);
			int height = Math.max(1, getHeight()-7);
			if (selected) {
				g.setColor(new Color(15, 23, 42, 26));
				g.fill(new RoundRectangle2D.Float(x, y+1, width, height, 13, 13));
				g.setColor(UITheme.ACCENT);
				g.fill(new RoundRectangle2D.Float(x, y, width, height, 13, 13));
				g.setColor(new Color(255, 255, 255, 55));
				g.setStroke(new BasicStroke(1f));
				g.draw(new RoundRectangle2D.Float(x+0.5f, y+0.5f, width-1, height-1, 13, 13));
			} else if (hover) {
				g.setColor(SHADOW);
				g.fill(new RoundRectangle2D.Float(x, y+1, width, height, 13, 13));
				g.setColor(HOVER_FILL);
				g.fill(new RoundRectangle2D.Float(x, y, width, height, 13, 13));
				g.setColor(HOVER_BORDER);
				g.setStroke(new BasicStroke(1f));
				g.draw(new RoundRectangle2D.Float(x+0.5f, y+0.5f, width-1, height-1, 13, 13));
			}
			g.dispose();
		}
	}

	private static String ellipsize(String text, FontMetrics metrics, int maxWidth) {
		if (text==null) return "";
		if (metrics==null || metrics.stringWidth(text)<=maxWidth) return text;
		String suffix = "\u2026";
		int end = text.length();
		while (end>0 && metrics.stringWidth(text.substring(0, end)+suffix)>maxWidth)
			end--;
		return text.substring(0, end)+suffix;
	}
}
