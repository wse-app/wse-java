package wse.utils.json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import wse.utils.internal.IParser;

public class JUtils {

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
		return raw;
	}

	public static String quoted(String key) {
		return "\"" + escape(key) + "\"";
	}

	protected static void addValueString(StringGatherer builder, int level, Object value) {
		if (value == null) {
			builder.add("null");
			return;
		}

		if (value instanceof JValue) {
			JValue jval = (JValue) value;

			jval.prettyPrint(builder, level);
			return;
		}

		if (value instanceof Number || value instanceof Boolean) {
			builder.add(String.valueOf(value));
			return;
		}
		
		builder.add("\"");
		builder.add(escape(String.valueOf(value)));
		builder.add("\"");
	}
	
	public static final IParser<JObject> JSON_PARSER = new IParser<JObject>() {
		public JObject parse(InputStream input, Charset cs) throws IOException {
			return JObject.parse(input, cs);
		}

		@Override
		public JObject createEmpty() {
			return new JObject();
		}
	};
}