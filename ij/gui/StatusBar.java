package ij.gui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/** Single-line status bar drawn directly on an AWT canvas. */
public class StatusBar extends Canvas {

	private static final long serialVersionUID = 1L;
	private static final int HEIGHT = 22;
	private static final int PADDING = 8;

	private String text = " ";

	public StatusBar() {
		setBackground(UITheme.STATUS);
		setForeground(UITheme.MUTED);
		setFont(UITheme.font(Font.PLAIN, 12));
	}

	public void setText(String text) {
		this.text = text!=null ? text : " ";
		repaint();
	}

	public String getText() {
		return text;
	}

	public Dimension getPreferredSize() {
		return new Dimension(10, HEIGHT);
	}

	public Dimension getMinimumSize() {
		return new Dimension(10, HEIGHT);
	}

	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		g.setColor(UITheme.STATUS);
		g.fillRect(0, 0, width, height);
		g.setColor(UITheme.BORDER);
		g.drawLine(0, 0, width, 0);
		g.setColor(UITheme.MUTED);
		g.setFont(getFont());
		FontMetrics metrics = g.getFontMetrics();
		int baseline = (height+metrics.getAscent()-metrics.getDescent())/2;
		g.setClip(0, 1, Math.max(0, width), Math.max(0, height-1));
		g.drawString(text, PADDING, baseline);
	}
}
