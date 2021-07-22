package wse.utils.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import wse.utils.StringUtils;
import wse.utils.http.StreamUtils;

public class JSONUtils {

	protected static final String[] levels = { "", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t\t",
			"\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t", };

	public static String level(int level) {
		return levels[(level) % levels.length];
	}

	public static String escape(String raw) {
		if (raw == null)
			return null;
		raw = raw.replace("\\", "\\\\").replace("\b", "\\b").replace("\f", "\\f").replace("\n", "\\n")
				.replace("\r", "\\r").replace("\t", "\\t").replace("\"", "\\\"");
//		raw = StringUtils.applyUnicodes(raw);
		return raw;
	}

	public static String unescape(String formatted) {
		formatted = formatted.replace("\\b", "\b").replace("\\f", "\f").replace("\\n", "\n").replace("\\r", "\r")
				.replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\");
		formatted = StringUtils.parseUnicodes(formatted);
		return formatted;
	}

	public static String keyString(String key) {
		return "\"" + escape(key) + "\"";
	}

	public static String valueString(Object value) {
		return valueString(value, 0);
	}

	public static String valueString(Object value, int level) {
		if (value instanceof JSONObject || value instanceof JSONArray) {
			return ((JSONValue) value).toString(level);
		} else if (value instanceof Number) {
			return String.valueOf(value);
		} else {
			return "\"" + escape(String.valueOf(value)) + "\"";
		}
	}

	protected static <T> T cast(Object value) {
		return cast(value, null);
	}

	@SuppressWarnings("unchecked")
	protected static <T> T cast(Object value, Class<T> clazz) {
		if (value == null)
			return null;
		if (clazz == null)
			return (T) value;

		try {
			return (T) clazz.getMethod("valueOf", String.class).invoke(null, value);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new JSONException(clazz.getName() + ".valueOf() failed: " + e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			return (T) value;
		} catch (InvocationTargetException e) {
			throw new JSONException(clazz.getName() + ".valueOf() failed: " + e.getCause().getMessage(), e.getCause());
		}
	}

	public static <T> T parse(InputStream source, String charsetName) throws IOException {
		return parse(source, Charset.forName(charsetName));
	}

	public static <T> T parse(InputStream source, Charset charset) throws IOException {
		return parse(new String(StreamUtils.readAll(source), charset));
	}

	@SuppressWarnings("unchecked")
	public static <T> T parse(String source) {

		source = trimv2(source);
		return (T) parseValue(source);
	}

	private static Object parseValue(String text) {
		
		System.out.println("Parsing: '" + text + "'");
		
//		int a = (text.charAt(0) - text.charAt(text.length() - 1));
//		if (a != -2 && a != 0) {
//			throw new JSONSyntaxException("Invalid JSON syntax, expected closing of '" + text.charAt(0) + "'");
//		}
		if (text == null || text.isEmpty())
			return null;

		switch (text.charAt(0)) {
		case '{': {
			JSONObject object = new JSONObject();
			String[] entries = splitComma(text.substring(1, text.length() - 1));
			for (String e : entries) {
				if (e.isEmpty())
					continue;
				String[] kv = splitEntry(e);
				object.put(unescape(kv[0]), parseValue(kv[1]));
			}
			return object;
		}
		case '[': {
			JSONArray array = new JSONArray();
			String[] elements = splitComma(text.substring(1, text.length() - 1));
			for (String e : elements) {
				if (e.isEmpty())
					continue;
				array.add(parseValue(e));
			}
			return array;
		}

		case '\"': {
			String res = unescape(text.substring(1, text.length() - 1));
			return res;
		}
		default:
			String res = unescape(text);
			return res;
		}
	}

	public static String[] splitComma(String part) {
		ArrayList<String> parts = new ArrayList<String>();

		{
			int lvl = 0;
			char[] src = part.toCharArray();

			int prev = 0;
			boolean inName = false, escaped = false, nextEscaped = false;
			char c;
			for (int i = 0; i < src.length; i++, escaped = nextEscaped, nextEscaped = false) {
				c = src[i];

				if (!escaped) {
					if (c == '\"') {
						inName = !inName;
					} else if (c == '\\' && inName) {
						nextEscaped = true;
					} else {
						if (c == '{' || c == '[') {
							lvl++;
						} else if (c == '}' || c == ']') {
							lvl--;
						} else if (c == ',' && lvl == 0) {
							parts.add(part.substring(prev, i));
							prev = i + 1;
						}
					}
				}
			}
			parts.add(part.substring(prev));
		}

		String[] res = new String[parts.size()];
		return parts.toArray(res);
	}

	public static String[] splitEntry(String s) {
		String[] kv = new String[2];

		char[] text = s.toCharArray();

		char c;
		boolean inName = false, escaped = false, nextEscaped = false;
		for (int i = 0; i < text.length; i++, escaped = nextEscaped, nextEscaped = false) {
			c = text[i];

			if (!escaped) {
				if (c == '\"') {
					inName = !inName;
				} else if (c == '\\' && inName) {
					nextEscaped = true;
				} else if (c == ':' && !inName) {
					kv[0] = s.substring(1, i - 1);
					kv[1] = s.substring(i + 1);
					return kv;
				}
			}
		}
		throw new JSONSyntaxException("JSON entry had no key/value: " + s);
	}

	public static String trimv2(String s) {
		if (s == null)
			return null;
		char[] text = s.toCharArray();
		char c;
		boolean inName = false, escaped = false, nextEscaped = false;
		int j = 0;
		for (int i = 0; i < text.length; i++, escaped = nextEscaped, nextEscaped = false) {
			c = text[i];

			if (!escaped) {
				if (c == '\"') {
					inName = !inName;
					text[j++] = c;
				} else if (c == '\\' && inName) {
					nextEscaped = true;
					text[j++] = c;
				} else {
					if (inName || (c != ' ' && c != '\t' && c != '\n' && c != '\r'))
						text[j++] = c;
				}
			} else {
				text[j++] = c;
			}
		}
		return new String(text, 0, j);
	}

}
