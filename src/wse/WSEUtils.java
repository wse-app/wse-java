package wse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import wse.utils.CallHandler;
import wse.utils.FileDataWriter;
import wse.utils.HttpHeaderWriter;
import wse.utils.HttpResult;
import wse.utils.HttpWriter;
import wse.utils.Protocol;
import wse.utils.exception.WseException;
import wse.utils.http.HttpMethod;
import wse.utils.ssl.SSLAuth;

class WSEUtils {

//	private static final Logger logger = Logger.getLogger(WSE.LOG_CLIENT_NAME);

	/*
	 * Get
	 */
	public static HttpResult get(String uri) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return get(new URI(uri), null, null);
	}
	
	public static HttpResult get(String uri, HttpHeaderWriter writer) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return get(new URI(uri), null, writer);
	}

	public static HttpResult get(String uri, SSLAuth auth) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return get(new URI(uri), auth, null);
	}
	
	public static HttpResult get(String uri, SSLAuth auth, HttpHeaderWriter writer) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return get(new URI(uri), auth, writer);
	}

	public static HttpResult get(URI uri, SSLAuth auth) throws WseException {
		return get(uri, auth, null);
	}
	
	public static HttpResult get(URI uri, SSLAuth auth, HttpHeaderWriter writer) throws WseException {
		return send(HttpMethod.GET, uri, writer, auth);
	}

	/*
	 * Put
	 */

	public static HttpResult put(String uri, File file) throws FileNotFoundException, URISyntaxException, WseException {
		return put(uri, file, null);
	}

	public static HttpResult put(String uri, File file, SSLAuth auth)
			throws FileNotFoundException, URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return put(new URI(uri), file, auth);
	}

	public static HttpResult put(URI uri, File file) throws FileNotFoundException, WseException {
		return put(uri, file, null);
	}

	public static HttpResult put(URI uri, File file, SSLAuth auth) throws FileNotFoundException, WseException {
		return put(uri, file != null ? new FileDataWriter(file) : (FileDataWriter) null, auth);
	}

	public static HttpResult put(String uri, InputStream stream) throws URISyntaxException, WseException {
		return put(uri, stream, null);
	}

	public static HttpResult put(String uri, InputStream stream, SSLAuth auth) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return put(new URI(uri), stream, auth);
	}

	public static HttpResult put(URI path, InputStream stream) throws WseException {
		return put(path, stream, null);
	}

	public static HttpResult put(URI path, InputStream stream, SSLAuth auth) throws WseException {
		notNull(stream, "Invalid Input: null");
		return put(path, stream != null ? new FileDataWriter(stream) : (FileDataWriter) null, auth);
	}

	public static HttpResult put(String uri, final InputStream stream, final long content_length)
			throws URISyntaxException, WseException {
		return put(uri, stream, content_length, null);
	}

	public static HttpResult put(String uri, final InputStream stream, final long content_length, SSLAuth auth)
			throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return put(new URI(uri), stream, content_length, auth);
	}

	public static HttpResult put(URI uri, final InputStream stream, final long content_length) throws WseException {
		return put(uri, stream, content_length, null);
	}

	public static HttpResult put(URI uri, final InputStream stream, final long content_length, SSLAuth auth)
			throws WseException {
		return put(uri, stream != null ? new FileDataWriter(stream, content_length) : (FileDataWriter) null, auth);
	}

	public static HttpResult put(String uri, HttpWriter writer) throws URISyntaxException, WseException {
		return put(uri, writer, null);
	}

	public static HttpResult put(String uri, HttpWriter writer, SSLAuth auth) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return put(new URI(uri), writer, null);
	}

	public static HttpResult put(URI path, HttpWriter writer, SSLAuth auth) throws WseException {
		return send(HttpMethod.PUT, path, writer, auth);
	}

	/*
	 * Post
	 */
	public static HttpResult post(String uri, HttpWriter writer) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return post(new URI(uri), writer, null);
	}

	public static HttpResult post(URI uri, HttpWriter writer, SSLAuth auth) throws WseException {
		return send(HttpMethod.POST, uri, writer, auth);
	}

	/*
	 * Delete
	 */

	public static HttpResult delete(String uri) throws URISyntaxException, WseException {
		return delete(uri, null);
	}

	public static HttpResult delete(String uri, SSLAuth auth) throws URISyntaxException, WseException {
		notNull(uri, "Invalid URI: null");
		return delete(new URI(uri), auth);
	}

	public static HttpResult delete(URI uri) throws WseException {
		return delete(uri, null);
	}

	public static HttpResult delete(URI uri, SSLAuth auth) throws WseException {
		return send(HttpMethod.DELETE, uri, null, auth);
	}

	/*
	 * Send any
	 */
//	private static HttpResult send(HttpMethod method, URI uri) {
//		return send(method, uri, null);
//	}

	public static HttpResult send(HttpMethod method, URI uri, HttpWriter writer, SSLAuth auth) throws WseException {
		notNull(method, "Invalid method: null");
		notNull(uri, "Invalid URI: null");

		Protocol protocol = Protocol.forName(uri.getScheme());
		notNull(protocol, "Invalid procotol: null");

		String host = uri.getHost();
		notNull(host, "Invalid host: null");

		CallHandler callHandler = new CallHandler(method, uri, writer, auth);
		return callHandler.call();
	}

	private static void notNull(Object obj, String warning) {
		if (obj == null)
			throw new IllegalArgumentException(warning);
	}
}
