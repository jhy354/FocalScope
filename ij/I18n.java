package ij;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/** Lightweight Chinese label lookup for the ImageJ user interface. */
public final class I18n {

	private static final Properties LABELS = new Properties();

	static {
		load();
	}

	private I18n() {
	}

	private static void load() {
		InputStream in = null;
		try {
			in = I18n.class.getResourceAsStream("/IJ_zh_CN.properties");
			if (in == null) return;
			final Reader reader = new InputStreamReader(in, "UTF-8");
			LABELS.load(reader);
		}
		catch (final Exception e) {
			// No translation file, or it could not be read: keep English labels.
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (final Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Returns the Chinese label for the given English text, or the original text
	 * when no translation is available.
	 */
	public static String label(final String text) {
		if (text == null || text.length() == 0) return text;
		if (text.length()==1 && !Character.isLetterOrDigit(text.charAt(0)))
			return text;
		String translated = LABELS.getProperty(text);
		String key = text.trim();
		if (translated == null) translated = LABELS.getProperty(key);
		if (translated == null && key.endsWith(":")) {
			translated = LABELS.getProperty(key.substring(0, key.length() - 1));
		}
		return translated != null ? translated : text;
	}

	/** Returns a translation for menu text, preserving a trailing shortcut hint. */
	public static String menuLabel(final String text) {
		if (text == null || text.length() == 0) return text;
		String translated = label(text);
		if (!translated.equals(text)) return translated;
		int separator = text.indexOf("  ");
		if (separator > 0) {
			String base = text.substring(0, separator);
			String baseTranslated = label(base);
			if (!baseTranslated.equals(base))
				return baseTranslated + text.substring(separator);
		}
		return text;
	}

	/**
	 * Translates hard-coded labels and menus of an already constructed window.
	 * AWT action commands are set back to the original English text so command
	 * dispatch and macros keep working.
	 */
	public static void translateWindow(final Window window) {
		if (window==null) return;
		if (window instanceof Frame)
			translateFrameTitle((Frame)window);
		else if (window instanceof Dialog)
			translateDialogTitle((Dialog)window);
		if (window instanceof Frame)
			translateMenuBar(((Frame)window).getMenuBar());
		if (window instanceof Container)
			translateContainer((Container)window);
	}

	private static void translateFrameTitle(final Frame frame) {
		String title = frame.getTitle();
		String translated = label(title);
		if (translated!=null && !translated.equals(title))
			frame.setTitle(translated);
	}

	private static void translateDialogTitle(final Dialog dialog) {
		String title = dialog.getTitle();
		String translated = label(title);
		if (translated!=null && !translated.equals(title))
			dialog.setTitle(translated);
	}

	private static void translateMenuBar(final MenuBar menuBar) {
		if (menuBar==null) return;
		for (int i=0; i<menuBar.getMenuCount(); i++)
			translateMenu(menuBar.getMenu(i));
	}

	private static void translateMenu(final Menu menu) {
		if (menu==null) return;
		String label = menu.getLabel();
		String translated = menuLabel(label);
		if (translated!=null && !translated.equals(label)) {
			String action = menu.getActionCommand();
			menu.setLabel(translated);
			if (action==null || action.equals(label))
				menu.setActionCommand(label);
		}
		for (int i=0; i<menu.getItemCount(); i++)
			translateMenuItem(menu.getItem(i));
	}

	private static void translateMenuItem(final MenuItem item) {
		if (item==null) return;
		if (item instanceof Menu) {
			translateMenu((Menu)item);
			return;
		}
		String label = item.getLabel();
		String translated = menuLabel(label);
		if (translated==null || translated.equals(label)) return;
		String action = item.getActionCommand();
		item.setLabel(translated);
		if (action==null || action.equals(label))
			item.setActionCommand(label);
	}

	private static void translateContainer(final Container container) {
		Component[] components = container.getComponents();
		for (Component component : components) {
			if (component instanceof Label) {
				Label label = (Label)component;
				String text = label.getText();
				String translated = text!=null?label(text):null;
				if (translated!=null && !translated.equals(text))
					label.setText(translated);
			} else if (component instanceof Button) {
				Button button = (Button)component;
				String text = button.getLabel();
				String translated = label(text);
				if (!translated.equals(text)) {
					String action = button.getActionCommand();
					button.setLabel(translated);
					if (action==null || action.equals(text))
						button.setActionCommand(text);
				}
			} else if (component instanceof Checkbox) {
				Checkbox checkbox = (Checkbox)component;
				String text = checkbox.getLabel();
				String translated = label(text);
				if (!translated.equals(text))
					checkbox.setLabel(translated);
			}
			if (component instanceof Container)
				translateContainer((Container)component);
		}
	}

}
