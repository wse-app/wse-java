package wse.utils.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import wse.utils.Supplier;
import wse.utils.writable.StreamWriter;

public class HttpHeader extends HttpAttributeList implements StreamWriter {

	private HttpDescriptionLine descLine;
	private boolean sendContentLength;

	public HttpHeader(HttpHeader copy) {
		super(copy);
		this.descLine = HttpDescriptionLine.copy(copy.descLine);
		this.sendContentLength = copy.sendContentLength;
	}

	public HttpHeader() {
	}

	public static HttpHeader read(String[] header_rows) {
		return new HttpHeader(header_rows);
	}

	public HttpHeader(String[] rows) {
		this(rows, 0, rows.length);
	}

	public HttpHeader(String[] rows, int off, int len) {
		super(rows, off + 1, len - 1);
		setDescriptionLine(HttpDescriptionLine.fromString(rows[off]));
	}

	public HttpHeader(HttpDescriptionLine statusLine) {
		this(statusLine, null);
	}

	public HttpHeader(HttpDescriptionLine statusLine, List<HeaderAttribute> attribs) {
		super(attribs);
		setDescriptionLine(statusLine);
	}

	public boolean isSendContentLength() {
		return sendContentLength;
	}

	public void setSendContentLength(boolean sendContentLength) {
		this.sendContentLength = sendContentLength;

		if (sendContentLength) {
			if (attributes.containsKey(TransferEncoding.KEY)) {
				setTransferEncoding(TransferEncoding.IDENTITY);
			}
		} else {
			removeAttribute(CONTENT_LENGTH);
		}
	}

	public void updateContentLength(Supplier<Long> length) {
		long l;
		if (sendContentLength && (l = (length != null ? length.get() : -1)) != -1) {
			setContentLength(l);
		} else {
			removeAttribute(CONTENT_LENGTH);
		}
	}

	public HttpDescriptionLine getDescriptionLine() {
		return descLine;
	}

	public HttpRequestLine getRequestLine() {
		if (descLine == null || !descLine.isRequest())
			return null;
		return (HttpRequestLine) descLine;
	}

	public HttpStatusLine getStatusLine() {
		if (descLine == null || !descLine.isResponse())
			return null;
		return (HttpStatusLine) descLine;
	}

	/**
	 * Returns the value of the query parameter with the specified name
	 * 
	 * @param name the name of the query parameter
	 * @return the value of the query parameter with the specified name if it
	 *         exists, null otherwise
	 */
	public String getQueryValue(String name) {
		if (this.isRequest())
			return getRequestURI().getQuery(name);
		return null;
	}

	/**
	 * Get all query parameter names as an immutable set
	 * 
	 * @return an immutable set containing the query parameter names, or empty set
	 *         if there are no parameters
	 */
	public Set<String> getQueryNames() {
		if (this.isRequest())
			return Collections.unmodifiableSet(getRequestURI().getQueryNames());
		return null;
	}

	public Set<Entry<String, String>> getQueryEntrySet() {
		if (this.isRequest())
			return getRequestURI().getQuery();
		return null;
	}

	/*
	 * Request Line
	 */

	/**
	 * Returns the path part of the url. URL structure: path?query#fragment
	 * 
	 * @return the path part of the url if there is one, null otherwise
	 */
	public String getRequestPath() {
		if (this.isRequest())
			return getRequestURI().getPath();
		return null;
	}

	public HttpURI getRequestURI() {
		if (this.isRequest())
			return getRequestLine().getURI();
		return null;
	}

	/**
	 * Returns the fragment part of the url. URL structure: path?query#fragment
	 * 
	 * @return the fragment part of the url if there is one, null otherwise
	 */
	public String getRequestURIFragment() {
		if (this.isRequest())
			return getRequestURI().getFragment();
		return null;
	}

	/**
	 * Returns the http version specified by the request line, normally HTTP/1.[0-2]
	 * 
	 * @return the http version specified by the request line
	 */
	public String getHttpVersion() {
		return getDescriptionLine().getHttpVersion();
	}

	public void setTransferEncoding(TransferEncoding enc) {
		super.setTransferEncoding(enc);
		if (enc != null && enc != TransferEncoding.IDENTITY)
			setSendContentLength(false);
	}

	public int length() {
		int result = 0;
		if (descLine != null)
			result += descLine.length() + 2; // CRLF
		return result + super.length(); // double CRLF

	}

	public String toPrettyString() {
		byte[] b = toByteArray();
		return new String(b, 0, Math.max(0, b.length - 2));
	}

	/**
	 * Contains double newline
	 */
	public String toString() {
		return new String(toByteArray());
	}

	/**
	 * Contains double newline
	 */
	public byte[] toByteArray() {
		byte[] b = new byte[length()];
		try {
			write(b, 0);
		} catch (Exception e) {
			System.out.println(new String(b));
			throw e;
		}
		return b;
	}

	/**
	 * Contains double newline
	 */
	public int write(byte[] dest, int off) {
		int p = off;
		if (descLine != null) {
			p += descLine.write(dest, p);
			dest[p++] = '\r';
			dest[p++] = '\n';
		}
		return (p - off) + super.write(dest, p);
	}

	/**
	 * Contains double newline
	 */
	@Override
	public void writeToStream(OutputStream stream, Charset cs) throws IOException {
		stream.write(toByteArray());
	}

	public void setDescriptionLine(HttpDescriptionLine descLine) {
		this.descLine = descLine;
	}

	public HttpMethod getMethod() {
		if (descLine == null)
			return null;

		if (descLine.isRequest())
			return ((HttpRequestLine) descLine).getMethod();

		return null;
	}

	public boolean isRequest() {
		if (descLine == null)
			return false;
		return descLine.isRequest();
	}

	public boolean isResponse() {
		if (descLine == null)
			return false;

		return descLine.isResponse();
	}

	public Charset getContentCharset() {
		ContentType ct = getContentType();
		if (ct == null) return null;
		return ct.getCharsetParsed();
	}
}
