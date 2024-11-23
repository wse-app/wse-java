package wse.utils.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import wse.utils.writable.StreamWriter;

/**
 * Enum containing constants for HTTP Methods, including POST, GET, PUT, HEAD,
 * DELETE, PATCH, OPTIONS and Secure
 *
 * @see #POST
 * @see #GET
 * @see #PUT
 * @see #HEAD
 * @see #DELETE
 * @see #PATCH
 * @see #OPTIONS
 * @see #SECURE
 *
 * @author Web Service Engine
 */
public enum HttpMethod implements StreamWriter {
	/**
	 * constant for the http method POST, usually associated with an exchange of
	 * data.
	 */
	POST("POST"),
	/**
	 * constant for the http method GET, usually associated with downloading data.
	 */
	GET("GET"),
	/** constant for the http method PUT, usually associated with uploading data. */
	PUT("PUT"),
	/**
	 * constant for the http method HEAD, the server should respond as if it were a
	 * GET request, but without any content.
	 */
	HEAD("HEAD"),
	/**
	 * constant for the http method DELETE, usually associated with deletion of
	 * server-side files.
	 */
	DELETE("DELETE"),
	/**
	 * constant for the http method PATCH, usually associated with modifying parts
	 * of existing resources on the server.
	 */
	PATCH("PATCH"),
	/**
	 * constant for the http method OPTIONS, the server should normally respond with
	 * allowed http Methods via the Allow header attribute.
	 */
	OPTIONS("OPTIONS"),
	/** constant for the shttp method Secure, used by the shttp */
	SECURE("Secure");

	public final String name;

	HttpMethod(String name) {
		this.name = name;
	}

	public static HttpMethod getMethodStrict(String header) {
		if (header == null)
			return null;

		for (HttpMethod m : HttpMethod.values()) {
			if (header.equals(m.name))
				return m;
		}

		return null;
	}

	public static HttpMethod getMethod(String header) {
		if (header == null)
			return null;

		for (HttpMethod m : HttpMethod.values()) {
			if (header.startsWith(m.name))
				return m;
		}

		return null;
	}

	public int length() {
		return name.getBytes().length;
	}

	public byte[] toByteArray() {
		byte[] b = new byte[length()];
		write(b, 0);
		return b;
	}

	public int write(byte[] dest, int off) {
		byte[] b = name.getBytes();
		System.arraycopy(b, 0, dest, off, b.length);
		return b.length;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset cs) throws IOException {
		stream.write(name.getBytes(cs));
	}

	@Override
	public String toString() {
		return name;
	}
}
