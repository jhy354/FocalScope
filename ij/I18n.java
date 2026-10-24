package ij;

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
		String translated = LABELS.getProperty(text);
		String key = text.trim();
		if (translated == null) translated = LABELS.getProperty(key);
		if (translated == null && key.endsWith(":")) {
			translated = LABELS.getProperty(key.substring(0, key.length() - 1));
		}
		return translated != null ? translated : text;
	}

}
