package wse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Objects;

import wse.utils.CallHandler;
import wse.utils.HttpHeaderWriter;
import wse.utils.HttpResult;
import wse.utils.Protocol;
import wse.utils.exception.WseException;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.ssl.SSLAuth;
import wse.utils.writer.FileDataWriter;
import wse.utils.writer.HttpWriter;
import wse.utils.writer.StringWriter;

class WSEUtils {

	/*
	 * Get
	 */
	public static HttpResult get(String uri) throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return get(new URI(uri), null, null);
	}

	public static HttpResult get(String uri, HttpHeaderWriter writer) throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return get(new URI(uri), null, writer);
	}

	public static HttpResult get(String uri, SSLAuth auth) throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return get(new URI(uri), auth, null);
	}

	public static HttpResult get(String uri, SSLAuth auth, HttpHeaderWriter writer)
			throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return get(new URI(uri), auth, writer);
	}

	public static HttpResult get(URI uri, SSLAuth auth) throws WseException {
		return get(uri, auth, null);
	}

	public static HttpResult get(URI uri, SSLAuth auth, final HttpHeaderWriter writer) throws WseException {
		return send(HttpMethod.GET, uri, new HttpWriter() {
			@Override
			public void prepareHeader(HttpHeader header) {
				if (writer != null)
					writer.prepareHeader(header);
			}

			@Override
			public void writeToStream(OutputStream stream, Charset charset) throws IOException {
			}

			@Override
			public long requestContentLength(Charset cs) {
				return 0;
			}
		}, auth);
	}

	/*
	 * Put
	 */

	public static HttpResult put(String uri, File file) throws FileNotFoundException, URISyntaxException, WseException {
		return put(uri, file, null);
	}

	public static HttpResult put(String uri, File file, SSLAuth auth)
			throws FileNotFoundException, URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return put(new URI(uri), file, auth);
	}

	public static HttpResult put(URI uri, File file) throws FileNotFoundException, WseException {
		return put(uri, file, null);
	}

	public static HttpResult put(URI uri, File file, SSLAuth auth) throws FileNotFoundException, WseException {
		return put(uri, file != null ? new FileDataWriter(file) : (FileDataWriter) null, auth);
	}

	public static HttpResult put(String uri, HttpWriter writer) throws URISyntaxException, WseException {
		return put(uri, writer, null);
	}

	public static HttpResult put(String uri, HttpWriter writer, SSLAuth auth) throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return put(new URI(uri), writer, null);
	}

	public static HttpResult put(URI path, HttpWriter writer, SSLAuth auth) throws WseException {
		return send(HttpMethod.PUT, path, writer, auth);
	}

	/*
	 * Post
	 */

	public static HttpResult post(String uri, Object data) throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return post(new URI(uri), new StringWriter(data == null ? null : data.toString()), null);
	}

	public static HttpResult post(String uri, HttpWriter writer) throws URISyntaxException, WseException {
		Objects.requireNonNull(uri, "Invalid URI: null");
		return post(new URI(uri), writer, null);
	}

	public static HttpResult post(URI uri, Object data, SSLAuth auth) throws WseException {
		return send(HttpMethod.POST, uri, new StringWriter(data == null ? null : data.toString()), auth);
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
		Objects.requireNonNull(uri, "Invalid URI: null");
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

	public static HttpResult send(HttpMethod method, URI uri, HttpWriter writer, SSLAuth auth) throws WseException {
		Objects.requireNonNull(method, "Invalid method: null");
		Objects.requireNonNull(uri, "Invalid URI: null");

		Protocol protocol = Protocol.forName(uri.getScheme());
		Objects.requireNonNull(protocol, "Invalid procotol: null");

		String host = uri.getHost();
		Objects.requireNonNull(host, "Invalid host: null");

		CallHandler callHandler = new CallHandler(method, uri, writer, auth);
		return callHandler.call();
	}
}
