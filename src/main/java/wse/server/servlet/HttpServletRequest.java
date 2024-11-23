package wse.server.servlet;

import java.io.InputStream;

import wse.server.RequestInfo;
import wse.utils.exception.WseParsingException;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpRequestLine;

public final class HttpServletRequest extends HttpHeader {
	private final InputStream content;
	private final RequestInfo connectorInfo;

	private HttpServletRequest(RequestInfo info, HttpHeader header, InputStream content) {
		super(header);

		this.connectorInfo = info;
		this.content = content;
	}

	public static HttpServletRequest make(HttpHeader header, RequestInfo info, InputStream content) {
		HttpRequestLine requestLine;

		if (header != null) {
			requestLine = header.getRequestLine();
			if (requestLine != null) {
				if (requestLine.getMethod() == null)
					throw new WseParsingException("Got null method");
				if (requestLine.getURI() == null)
					throw new WseParsingException("Got null uri");

				if (requestLine.getHttpVersion() == null)
					throw new WseParsingException("Got null HttpVersion");
			} else
				throw new WseParsingException("Got null requestLine");
		} else
			throw new WseParsingException("Got null header");
		return new HttpServletRequest(info, header, content);
	}

	/*
	 * Parameters for REST API
	 */

	/*
	 * Http Message
	 */

	/**
	 * Returns the http content as an input stream, can only be read once. The
	 * InputStream returned from this method can only be read once, but multiple
	 * calls to this will return new InputStreams that can be read again.
	 * 
	 * @return the http content as an InputStream or null if no http content is
	 *         present
	 */
	public InputStream getContent() {
		return content;
	}

	/**
	 * Returns an object containing some information about the current session.
	 * 
	 * @return
	 */
	public RequestInfo getRequestInfo() {
		return connectorInfo;
	}

	/*
	 * Utils
	 */

//
//	/**
//	 * Parses the "Cookie" attribute and returns an immutable map containing
//	 * String:String entries that represent the attribute value. <br>
//	 * 
//	 * The Cookie HTTP request header contains stored HTTP cookies previously sent
//	 * by the server with the Set-Cookie header. <br>
//	 * <br>
//	 * Example: <br>
//	 * <code>Cookie: PHPSESSID=298zf09hf012fh2; csrftoken=u32t4o3tb3gg43; _gat=1;</code><br>
//	 * <br>
//	 * Send multiple cookies easily with the sendCookie(); method in
//	 * HttpServletResponse.
//	 * 
//	 * @return
//	 */
//	public Map<String, String> getCookies() {
//		return getStringMap("Cookie");
//	}

}
