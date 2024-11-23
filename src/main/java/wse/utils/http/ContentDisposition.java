package wse.utils.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import wse.utils.ArrayUtils;
import wse.utils.Transformer;

public class ContentDisposition {

	public static ContentDisposition attachment() {
		return attachment(null);
	}

	public static ContentDisposition attachment(String filename) {
		return new ContentDisposition("attachment", null, filename);
	}

	public String type;

	private Map<String, String> args = new HashMap<>();

	public static ContentDisposition fromAttributes(HttpAttributeList list) {
		HeaderAttribute a = list.getAttribute("Content-Disposition");
		if (a == null) {
			return new ContentDisposition(null);
		}
		return new ContentDisposition(a.value);
	}

	public ContentDisposition(String value) {
		if (value == null)
			return;
		String[] split = value.split(";");
		this.type = split[0];

		for (int i = 1; i < split.length; i++) {

			String[] kv = split[i].split("=", 2);
			String key = kv[0].trim();
			String val = kv.length > 1 ? kv[1].trim() : key;
			val = val.replace("\"", "");
			args.put(key.toLowerCase(), val);
		}
	}

	public ContentDisposition(String type, String name) {
		this.type = type;
		if (name != null)
			args.put("name", name);
	}

	public ContentDisposition(String type, String name, String filename) {
		this.type = type;
		if (name != null)
			args.put("name", name);
		if (filename != null)
			args.put("filename", filename);
	}

	public String getName() {
		return get("name");
	}

	public String getFilename() {
		return get("filename");
	}

	public ContentDisposition and(String key, String value) {
		args.put(key, value);
		return this;
	}

	public ContentDisposition filename(String filename) {
		return and("filename", filename);
	}

	public String get(String key) {
		return args.get(key);
	}

	public String getType() {
		return type;
	}

	public boolean is(String type) {
		if (this.type == null)
			return false;
		if (type == null)
			return false;
		if (type.equals(this.type))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return type + "; " + ArrayUtils.join(args.entrySet(), "; ", new Transformer<Entry<String, String>, String>() {
			@Override
			public String transform(Entry<String, String> value) {
				return value.getKey() + "=\"" + value.getValue() + "\"";
			}
		});
	}

}
