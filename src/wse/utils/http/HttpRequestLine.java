package wse.utils.http;

import wse.utils.exception.WseHttpException;
import wse.utils.json.StringGatherer;

public class HttpRequestLine extends HttpDescriptionLine {
	private HttpMethod method;
	private HttpURI uri;

	public HttpRequestLine(HttpRequestLine copy) {
		if (copy == null)
			throw new NullPointerException("Can't copy null object");
		
		this.method = copy.method;
		this.uri = new HttpURI(copy.uri);
	}
	
	public static HttpRequestLine fromString(String requestLine) {
		
		if (requestLine == null)
			return null;

		String[] parts = requestLine.split(" ");
		if (parts == null || parts.length != 3)
			return null;
		
		HttpMethod method = HttpMethod.getMethodStrict(parts[0]);
		if (method == null)
			throw new WseHttpException("Invalid Method: " + parts[0], 400);
		
		
		HttpURI uri = HttpURI.fromURI(parts[1]);
		if (uri == null)
			throw new WseHttpException("Invalid URI: " + parts[1], 400);
		

		return new HttpRequestLine(method, uri, parts[2].trim());
	}

	public HttpRequestLine(HttpMethod method, HttpURI uri) {
		this(method, uri, HttpDescriptionLine.HTTP11);
		
	}

	public HttpRequestLine(HttpMethod method, HttpURI uri, String httpVersion) {
		super(httpVersion);
		
		if (method == null)
			throw new IllegalArgumentException("Method can't be null");
		if (uri == null)
			throw new IllegalArgumentException("Http uri can't be null");

		setMethod(method);
		setUri(uri);
		
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpURI getURI() {
		return uri;
	}

	public void setUri(HttpURI uri) {
		this.uri = uri;
	}

	public void setUri(String uri) {
		setUri(HttpURI.fromURI(uri));
	}

	
	
	public void prettyPrint(StringGatherer builder, int level) {
		builder.add(method.toString());
		builder.add(" ");
		uri.prettyPrint(builder, level);
		builder.add(" ");
		builder.add(httpVersion);
	}

	public int length() {
		return method.length() + uri.length() + httpVersion.length() + 2;
	}

	
	public byte[] toByteArray() {
		byte[] b = new byte[length()];
		write(b, 0);
		return b;
	}

	public int write(byte[] dest, int off) {
		int p = off;
		p += method.write(dest, p);
		dest[p++] = ' ';
		p += uri.write(dest, p);
		dest[p++] = ' ';
		System.arraycopy(httpVersion.getBytes(), 0, dest, p, httpVersion.length());
		p += httpVersion.length();
		return p - off;
	}

}
