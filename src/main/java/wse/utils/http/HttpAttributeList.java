package wse.utils.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wse.WSE;
import wse.utils.ArrayUtils;
import wse.utils.MimeType;
import wse.utils.StringUtils;
import wse.utils.Transformer;
import wse.utils.writable.StreamWriter;

public class HttpAttributeList implements Map<String, String>, StreamWriter {

	protected static final String CONTENT_LENGTH = "Content-Length";
	protected static final String FROM = "From";
	protected static final String USER_AGENT = "User-Agent";
	protected static final String HOST = "Host";

	protected Map<String, HeaderAttribute> attributes = new LinkedHashMap<>();

	public HttpAttributeList(HttpAttributeList copy) {
		if (copy != null)
			for (HeaderAttribute a : copy.attributes.values()) {
				this.setAttribute(a.name, a.value);
			}
	}

	public HttpAttributeList() {
	}

	public HttpAttributeList(String header) {
		this(header.split("\r\n"));
	}

	public HttpAttributeList(String[] rows) {
		this(rows, 0, rows.length);
	}

	public HttpAttributeList(String[] rows, int offset, int len) {
		this();
		HeaderAttribute last = null;
		if (rows != null)
			for (int i = offset; i < offset + len; i++) {
				if (rows[i].length() > 0) {
					if (rows[i].charAt(0) == ' ') {
						if (last != null) {
							last.value += "\r\n" + rows[i].substring(1);
							this.setAttribute(last);
						}
					} else {
						if (last != null)
							this.setAttribute(last);
						HeaderAttribute attrib = new HeaderAttribute(rows[i]);
						last = attrib;
					}
				}
			}
		if (last != null)
			this.setAttribute(last);
	}

	public HttpAttributeList(List<HeaderAttribute> attributes) {
		this();
		if (attributes != null)
			for (HeaderAttribute attrib : attributes) {
				this.setAttribute(attrib.name, attrib.value);
			}
	}

	public Set<String> getAttributeNames() {
		return Collections.unmodifiableSet(attributes.keySet());
	}

	public HeaderAttribute getAttribute(String name) {
		return attributes.get(String.valueOf(name).toLowerCase());
	}

	public String getAttributeValue(String name) {
		HeaderAttribute a = getAttribute(name);
		if (a == null)
			return null;
		return a.value;
	}

	public HeaderAttribute setAttribute(String name, String value) {
		if (value == null) {
			removeAttribute(name);
			return null;
		}
		return setAttribute(new HeaderAttribute(name, value));
	}

	public HeaderAttribute setAttribute(HeaderAttribute attrib) {
		return this.attributes.put(String.valueOf(attrib.name).toLowerCase(), attrib);
	}

	public HeaderAttribute removeAttribute(String name) {
		return attributes.remove(String.valueOf(name).toLowerCase());
	}

	public Credentials getAuthorization() {
		return Credentials.fromHeader(this);
	}

	public void setAuthorizationBasic(Credentials cred) {
		this.setAttribute(Credentials.HEADER_ATTRIB_KEY, "Basic " + WSE.printBase64Binary(cred.toByteArray()));
	}

	public void setAuthorizationBearer(String token) {
		this.setAttribute(Credentials.HEADER_ATTRIB_KEY, "Bearer " + token);
	}

	public void setAuthorization(String authentication) {
		this.setAttribute(Credentials.HEADER_ATTRIB_KEY, authentication);
	}

	public TransferEncoding getTransferEncoding() {
		return TransferEncoding.fromName(getAttributeValue(TransferEncoding.KEY));
	}

	public void setTransferEncoding(TransferEncoding enc) {
		if (enc == null)
			removeAttribute(TransferEncoding.KEY);
		else
			setAttribute(TransferEncoding.KEY, enc.name);
	}

	public ContentType getContentType() {
		return ContentType.fromAttributes(this);
	}

	public String getContentTypeRaw() {
		return getAttributeValue("Content-Type");
	}

	public void setContentType(MimeType type) {
		if (type != null) {
			setContentType(type.toString());
		} else {
			removeAttribute("Content-Type");
		}
	}

	public void setContentType(String type) {
		setAttribute("Content-Type", type);
	}

	public ContentDisposition getContentDisposition() {
		return ContentDisposition.fromAttributes(this);
	}

	public String getContentDispositionRaw() {
		return getAttributeValue("Content-Disposition");
	}

	/**
	 * Returns the value of header attribute Content-Length or -1 if there is no
	 * attribute
	 */
	public long getContentLength() {
		try {
			String val = getAttributeValue(CONTENT_LENGTH);
			if (val == null)
				return -1;
			return Long.parseLong(val);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public void setContentLength(long length) {
		setAttribute(CONTENT_LENGTH, String.valueOf(length));
	}

	public void setAllow(String allow) {
		setAttribute("Allow", allow);
	}

	public void setAllow(HttpMethod... methods) {
		setAllow(ArrayUtils.join(methods, ", "));
	}

	public void setAccept(String accept) {
		setAttribute("Accept", accept);
	}

	public void setAccept(MimeType... types) {
		setAccept(ArrayUtils.join(types, ","));
	}

	public void setAccept(Map<MimeType, Float> encodings) {
		setAcceptEncoding(ArrayUtils.join(encodings.entrySet(), ",", new Transformer<Entry<MimeType, Float>, String>() {
			@Override
			public String transform(Entry<MimeType, Float> value) {

				float val = value.getValue();
				val = Math.round(val * 10) / 10;
				return value.getKey().toString() + ((val == 1) ? "" : ";q=" + val);
			}
		}));
	}

	public void setAcceptEncoding(String accept) {
		setAttribute("Accept-Encoding", accept);
	}

	public void setAcceptEncoding(TransferEncoding... encodings) {
		setAcceptEncoding(ArrayUtils.join(encodings, ","));
	}

	public void setAcceptEncoding(Map<TransferEncoding, Float> encodings) {
		setAcceptEncoding(
				ArrayUtils.join(encodings.entrySet(), ",", new Transformer<Entry<TransferEncoding, Float>, String>() {
					@Override
					public String transform(Entry<TransferEncoding, Float> value) {

						float val = value.getValue();
						val = Math.round(val * 10) / 10;
						return value.getKey().toString() + ((val == 1) ? "" : ";q=" + val);
					}
				}));
	}

	public void setAcceptCharset(String accept) {
		setAttribute("Accept-Charset", accept);
	}

	public void setAcceptCharset(String... accept) {
		setAcceptCharset(ArrayUtils.join(accept, ","));
	}

	public void setAcceptCharset(Charset... encodings) {
		setAcceptEncoding(ArrayUtils.join(encodings, ","));
	}

	public void setAcceptCharset(Map<Charset, Float> encodings) {
		setAcceptEncoding(ArrayUtils.join(encodings.entrySet(), ",", new Transformer<Entry<Charset, Float>, String>() {
			@Override
			public String transform(Entry<Charset, Float> value) {

				float val = value.getValue();
				val = Math.round(val * 10) / 10;
				return value.getKey().toString() + ((val == 1) ? "" : ";q=" + val);
			}
		}));
	}

	public void setFrom(String from) {
		setAttribute(FROM, from);
	}

	public void setUserAgent(String userAgent) {
		setAttribute(USER_AGENT, userAgent);
	}

	public void setHost(String host) {
		setAttribute(HOST, host);
	}

	/**
	 * Parses the "Accept-Language" attribute and returns an immutable list
	 * containing WeighedStrings the representing attribute value. <br>
	 * <br>
	 * Example: <br>
	 * <code>Accept-Language: en-NZ,en-GB;q=0.9,en-US;q=0.8,en;q=0.7</code>
	 * 
	 * @return an immutable list containing WeighedStrings representing the
	 *         "Accept-Language" attribute values. Result is sorted with respect to
	 *         weight.
	 * 
	 */
	public List<WeighedString> getAcceptLanguage() {
		return getWeighedStringList("Accept-Language");
	}

	/**
	 * Parses the "Accept-Charset" attribute and returns an immutable list
	 * containing WeighedStrings the representing attribute value. <br>
	 * <br>
	 * Example: <br>
	 * <code>Accept-Charset: utf-8, iso-8859-1;q=0.5</code>
	 * 
	 * @return an immutable list containing WeighedStrings representing the
	 *         "Accept-Charset" attribute values. Result is sorted with respect to
	 *         weight.
	 * 
	 */
	public List<WeighedString> getAcceptCharset() {
		return getWeighedStringList("Accept-Charset");
	}

	/**
	 * Parses the "Accept-Encoding" attribute and returns an immutable list
	 * containing WeighedTransferEncodings representing the attribute value. <br>
	 * <br>
	 * Example: <br>
	 * Accept-Encoding: identity;q=0.5,gzip;q=1 <br>
	 * <br>
	 * WSE currently only supports identity, if identity weight is 0, "406 Not
	 * Acceptable" will be returned immidiately.
	 * 
	 * @return an immutable list containing WeighedStrings representing the
	 *         "Accept-Encoding" attribute value.
	 */
	public List<WeighedTransferEncoding> getAcceptEncoding() {
		String val = getAttributeValue("Accept-Language");
		if (val == null)
			return Collections.emptyList();

		List<WeighedTransferEncoding> result = new ArrayList<>();

		if (val.contains(",")) {
			for (String s : val.split(",")) {
				WeighedTransferEncoding e = WeighedTransferEncoding.fromText(s);
				if (e != null)
					result.add(e);
			}
		} else {
			WeighedTransferEncoding e = WeighedTransferEncoding.fromText(val);
			if (e != null)
				result.add(e);
		}

		Collections.sort(result);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Parses the "Accept" attribute and returns an immutable list containing
	 * WeighedStrings representing the attribute value. <br>
	 * <br>
	 * Example: <br>
	 * <code>Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*</code><code>/</code><code>*;q=0.8,application/signed-exchange;v=b3</code>
	 * 
	 * @return an immutable list containing WeighedStrings representing the "Accept"
	 *         attribute values. Result is sorted with respect to weight.
	 * 
	 */
	public List<WeighedString> getAccept() {
		return getWeighedStringList("Accept");
	}

	public double accepts(MimeType mimeType) {
		for (WeighedString ws : getAccept()) {
			MimeType mt = MimeType.getByName(ws.string);
			if (mt == null)
				continue;
			if (mt.contains(mimeType)) {
				return ws.q;
			}
		}
		return 0;
	}

	/**
	 * Parses the specified attribute and returns an immutable list containing
	 * WeighedStrings representing the attribute value. <br>
	 * <br>
	 * Example: <br>
	 * <code>Accept-Encoding: identity;q=0.5,gzip;q=1</code><br>
	 * <code>Accept-Language: en-NZ,en-GB;q=0.9,en-US;q=0.8,en;q=0.7</code>
	 * 
	 * @return an immutable list containing WeighedStrings representing the
	 *         attribute value. Result is sorted with respect to weight.
	 * 
	 */
	public List<WeighedString> getWeighedStringList(String attribute_name) {
		String val = getAttributeValue(attribute_name);
		if (val == null)
			return Collections.emptyList();

		List<String> parts = parseStringList(val, ',', true, false);
		List<WeighedString> result = new ArrayList<>(parts.size());

		for (String s : parts)
			result.add(new WeighedString(s));

		Collections.sort(result);
		return Collections.unmodifiableList(result);
	}

	public Map<String, String> getStringMap(String attributeName, char entrySeparator, char keyValueSeparator,
			boolean trim) {
		String val = getAttributeValue(attributeName);
		return parseStringMap(val, entrySeparator, keyValueSeparator, trim);
	}

	public static Map<String, String> parseStringMap(String val, char entrySeparator, char keyValueSeparator,
			boolean trim) {
		if (val == null)
			return Collections.emptyMap();

		Map<String, String> result = new LinkedHashMap<>();

		List<String> entries = StringUtils.split(val, entrySeparator);

		for (String entry : entries) {

			List<String> keyValue = StringUtils.split(entry, keyValueSeparator, 2);

			if (keyValue.size() == 2) {
				String key = keyValue.get(0);
				String value = keyValue.get(1);

				if (trim) {
					key = key.trim();
					value = value.trim();
				}

				result.put(keyValue.get(0).trim(), keyValue.get(1).trim());
			} else if (keyValue.size() == 1) {
				String key = keyValue.get(0);
				if (trim) {
					key = key.trim();
				}
				result.put(key, key);
			}
		}
		return result;
	}

	public List<String> getStringList(String attributeName, char entrySeparator, boolean trim, boolean lowerCase) {
		String val = getAttributeValue(attributeName);
		return parseStringList(val, entrySeparator, trim, lowerCase);
	}

	public static List<String> parseStringList(String val, char entrySeparator, boolean trim, boolean lowerCase) {
		if (val == null)
			return Collections.emptyList();

		List<String> split = StringUtils.split(val, entrySeparator);
		if (!trim && !lowerCase)
			return split;

		List<String> result = new LinkedList<>();
		for (String s : split) {
			if (trim)
				s = s.trim();
			if (lowerCase)
				s = s.toLowerCase();
			result.add(s);
		}

		return result;
	}

	public static String printStringMap(Map<String, String> map, char entrySeparator, char keyValueSeparator) {
		if (map.size() == 0)
			return "";

		StringBuilder builder = new StringBuilder();

		Iterator<Entry<String, String>> entryIt = map.entrySet().iterator();

		Entry<String, String> entry = entryIt.next();
		builder.append(String.format("%s%c%s", entry.getKey(), keyValueSeparator, entry.getValue()));

		while (entryIt.hasNext()) {
			builder.append(entrySeparator);
			entry = entryIt.next();
			builder.append(String.format("%s%c%s", entry.getKey(), keyValueSeparator, entry.getValue()));
		}
		return builder.toString();
	}

	public List<String> getConnection(boolean lowerCase) {
		return getStringList("Connection", ',', true, lowerCase);
	}

	public void setConnection(String... connection) {
		setConnection(ArrayUtils.join(connection, ", "));
	}

	public void setConnection(String connection) {
		setAttribute("Connection", connection);
	}

	public Map<String, String> getKeepAlive() {
		return getStringMap("Keep-Alive", ',', '=', true);
	}

	public void setKeepAlive(Integer timeout, Integer max) {
		if (timeout == null && max == null)
			removeAttribute("keep-alive");

		Map<String, String> map = new HashMap<>();
		if (timeout != null)
			map.put("timeout", String.valueOf(timeout));
		if (max != null)
			map.put("max", String.valueOf(max));
		setKeepAlive(printStringMap(map, ',', '='));
	}

	public void setKeepAlive(String keepAlive) {
		setAttribute("Keep-Alive", keepAlive);
	}

	/** Get the "Sec-Fetch-Mode header" attribute */
	public String getFetchDest() {
		return getAttributeValue("Sec-Fetch-Dest");
	}

	/** Get the "Sec-Fetch-Mode" header attribute */
	public String getFetchMode() {
		return getAttributeValue("Sec-Fetch-Mode");
	}

	/** Get the "Sec-Fetch-Mode" header attribute */
	public String getFetchSite() {
		return getAttributeValue("Sec-Fetch-Site");
	}

	/** Get the "Sec-Fetch-Mode" header attribute */
	public String getFetchUser() {
		return getAttributeValue("Sec-Fetch-User");
	}

	@Override
	public void writeToStream(OutputStream stream, Charset cs) throws IOException {
		stream.write(toByteArray());
	}

	public byte[] toByteArray() {
		byte[] b = new byte[length()];
		write(b, 0);
		return b;
	}

	/**
	 * Contains double newline
	 */
	@Deprecated
	public int write(byte[] dest, int off) {
		int p = off;

		for (HeaderAttribute ha : attributes.values()) {
			p += ha.write(dest, p);
		}

		dest[p++] = '\r';
		dest[p++] = '\n';
		return p - off;
	}

	public int length() {
		int result = 0;
		for (HeaderAttribute a : this.attributes.values()) {
			result += a.length(); // contains CRLF
		}
		return result + 2; // double CRLF

	}

	@Override
	public String toString() {
		return new String(toByteArray());
	}

	@Override
	public int size() {
		return attributes.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return attributes.containsKey(String.valueOf(key).toLowerCase());
	}

	@Override
	public boolean containsValue(Object value) {
		if (value == null)
			return false;
		for (HeaderAttribute attrib : this.attributes.values()) {
			if (value.equals(attrib.value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String get(Object key) {
		if (key == null)
			return null;
		return getAttributeValue(String.valueOf(key).toLowerCase());
	}

	@Override
	public String put(String key, String value) {
		HeaderAttribute attrib = this.setAttribute(key, value);
		return attrib != null ? attrib.value : null;
	}

	@Override
	public String remove(Object key) {
		if (key == null)
			return null;
		HeaderAttribute attrib = this.removeAttribute(String.valueOf(key));
		return attrib != null ? attrib.value : null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		for (Entry<? extends String, ? extends String> e : m.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		this.attributes.clear();
	}

	@Override
	public Set<String> keySet() {
		return this.attributes.keySet();
	}

	@Override
	public Collection<String> values() {
		List<String> values = new ArrayList<>(this.size());
		for (HeaderAttribute a : this.attributes.values()) {
			values.add(a.value);
		}
		return values;
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		Set<Entry<String, String>> res = new HashSet<>();
		for (final Entry<String, HeaderAttribute> e : attributes.entrySet()) {
			res.add(new Entry<String, String>() {
				@Override
				public String setValue(String value) {
					String old = e.getValue().value;
					e.getValue().value = value;
					return old;
				}

				@Override
				public String getValue() {
					return e.getValue().value;
				}

				@Override
				public String getKey() {
					return e.getKey();
				}
			});
		}
		return res;
	}

	public Charset getContentCharset() {
		ContentType ct = getContentType();
		if (ct == null)
			return null;
		return ct.getCharsetParsed();
	}
}
