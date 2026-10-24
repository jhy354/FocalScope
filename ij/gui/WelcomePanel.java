package ij.gui;

import ij.IJ;
import ij.ImageJ;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Modern empty-state shown before the first image is opened. */
public class WelcomePanel extends Canvas {

	private static final long serialVersionUID = 1L;
	private final Image logo;
	private final Rectangle openButton = new Rectangle();
	private boolean hover;

	public WelcomePanel() {
		setBackground(UITheme.BACKGROUND);
		logo = loadLogo();
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (openButton.contains(e.getPoint()))
					IJ.run("Open...");
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				boolean inside = openButton.contains(e.getPoint());
				if (inside != hover) {
					hover = inside;
					setCursor(Cursor.getPredefinedCursor(inside ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
					repaint();
				}
			}
		});
	}

	private Image loadLogo() {
		try {
			java.net.URL url = WelcomePanel.class.getResource("/ImageX.png");
			return url != null ? Toolkit.getDefaultToolkit().getImage(url) : null;
		} catch (Exception e) {
			return null;
		}
	}

	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight();
		g.setColor(UITheme.BACKGROUND);
		g.fillRect(0, 0, w, h);

		int centerX = w / 2;
		int logoSize = 96;
		int logoY = Math.max(40, h / 2 - 190);
		if (logo != null)
			g.drawImage(logo, centerX - logoSize / 2, logoY, logoSize, logoSize, this);

		g.setColor(UITheme.TEXT);
		g.setFont(UITheme.font(Font.BOLD, 34));
		drawCentered(g, "ImageX", centerX, logoY + logoSize + 52);

		g.setColor(UITheme.MUTED);
		g.setFont(UITheme.font(Font.PLAIN, 14));
		drawCentered(g, "现代图像处理", centerX, logoY + logoSize + 78);

		int bw = 150;
		int bh = 40;
		int bx = centerX - bw / 2;
		int by = logoY + logoSize + 112;
		openButton.setBounds(bx, by, bw, bh);
		g.setColor(hover ? UITheme.ACCENT_HOVER : UITheme.ACCENT);
		g.fillRoundRect(bx, by, bw, bh, 14, 14);
		g.setColor(Color.WHITE);
		g.setFont(UITheme.font(Font.BOLD, 14));
		drawCentered(g, "打开图像", centerX, by + 26);

		g.setColor(UITheme.MUTED);
		g.setFont(UITheme.font(Font.PLAIN, 12));
		drawCentered(g, "将图像拖放到此窗口，或点击“打开图像”", centerX, by + 72);
		drawCentered(g, "ImageX " + ImageJ.VERSION, centerX, h - 24);
	}

	private void drawCentered(Graphics g, String text, int centerX, int baseline) {
		FontMetrics metrics = g.getFontMetrics();
		g.drawString(text, centerX - metrics.stringWidth(text) / 2, baseline);
	}
}
