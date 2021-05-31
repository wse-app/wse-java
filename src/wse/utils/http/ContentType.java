package wse.utils.http;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import wse.utils.ArrayUtils;
import wse.utils.MimeType;

public class ContentType {
	
	public String mimeType;
	
	private Map<String, String> args = new HashMap<>();
	
	public static ContentType fromAttributes(HttpAttributeList list) {
		HeaderAttribute a = list.getAttribute("Content-Type");
		if (a == null) {
			return new ContentType(null);
		}
		return new ContentType(a.value);
	}
	
	public ContentType(String value) {
		if (value == null)
			return;
		String[] split = value.split(";");
		this.mimeType = split[0];
		
		for (int i = 1; i < split.length; i++) {
			
			String[] kv = split[i].split("=", 2);
			String key = kv[0].trim();
			String val = kv.length > 1 ? kv[1].trim() : key;
			args.put(key.toLowerCase(), val);
		}
	}
	
	public ContentType(String mimetype, String charset) {
		this.mimeType = mimetype;
		args.put("charset", charset);
	}
	
	public ContentType(MimeType mimetype, String charset) {
		this(mimetype.toString());
	}
	
	public ContentType(MimeType mimetype, String charset, String boundary) {
		this(mimetype, charset);
		args.put("boundary", boundary);
	}
	
	public Charset getCharsetParsed() {
		try {
			return Charset.forName(getCharset());			
		}catch (Exception e) {
			return null;
		}
	}
	
	public void setCharset(String charset) {
		args.put("charset", charset);
	}
	
	public void setCharset(Charset charset) {
		args.put("charset", charset.toString());
	}
	
	public void setBoundary(String boundary) {
		args.put("boundary", boundary);
	}
	
	public String getCharset() {
		return args.get("charset");
	}
	
	public String getBoundary() {
		return args.get("boundary");
	}
	
	public String get(String key) {
		return args.get(key);
	}
	
	
	public String getMimeType() {
		return mimeType;
	}
	
	public MimeType parseType() {
		return MimeType.getByName(mimeType);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public boolean is(MimeType type) {
		if (this.mimeType == null)
			return false;
		if (type == null)
			return false;
		if (type.toString().equals(this.mimeType.toString()))
			return true;
		if (type.getSubName().equals("*")) {
			if (type.getName().equals("*"))
				return true;
			return this.mimeType.startsWith(type.getName());
		}
		return type.equals(mimeType);
	}
	
	@Override
	public String toString() {
		return mimeType + "; " + ArrayUtils.join(args, "; ", "=");
	}
	
}
