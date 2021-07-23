package wse.client.shttp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.client.HttpConnection;
import wse.utils.HttpResult;
import wse.utils.SHttp;
import wse.utils.StringUtils;
import wse.utils.exception.WseConnectionException;
import wse.utils.exception.WseSHttpException;
import wse.utils.exception.WseSHttpInitException;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpStatusLine;
import wse.utils.http.StreamUtils;
import wse.utils.log.Loggers;
import wse.utils.shttp.SKey;
import wse.utils.ssl.SSLAuth;

public final class SHttpManager {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	public static final int CIPHER_KEY_SIZE = 128;

	// private static byte[] iv =
	// { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	// 0x00, 0x00, 0x00, 0x00 };
	// private static AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);

	private SHttpManager() {
	}

	private static HashMap<String, SKey> store = new HashMap<String, SKey>();

	private static String toStoreName(SKey key) {
		return toStoreName(key.getInitHost(), key.getInitPort());
	}

	private static String toStoreName(String host, int port) {
		return host + port;
	}

	private static void store(String host, int port, SKey key) {
		store.put(toStoreName(host, port), key);
	}

	/**
	 * Invalidates the key, meaning further calls to its destination must aquire a
	 * new key.
	 * 
	 * @param key
	 */
	public static void invalidate(SKey key) {
		store.remove(toStoreName(key));
	}

	/**
	 * Invalidates the target, meaning any potential keys to it are removed.
	 * 
	 * @param host
	 * @param port
	 */
	public static void invalidate(String host, int port) {
		store.remove(toStoreName(host, port));
	}

	/**
	 * Makes sure the connection to initialize- host and port exists. If not, a new
	 * key is aquired and stored. The key contains port to the actual call
	 * destination, while host remains the same.
	 * 
	 * @param host
	 * @param port
	 */
	public static SKey getKey(final SSLAuth auth, final String host, final int port, final Logger logger) {
		SKey key = getAquiredKey(host, port);

		if (key != null && !key.hasExpired()) {
			// Still useable
			return key;
		}

		// Needs renewal
		Loggers.trace(logger, "aquireNewShttpKey()", new Runnable() {
			@Override
			public void run() {
				aquireNewShttpKey(auth, host, port, logger);
			}
		});

		key = getAquiredKey(host, port);
		return key;
	}

	/**
	 * 
	 * Finds the key associated with the specified host and port. If it doesn't
	 * exist, null is returned.
	 * 
	 * @param host
	 * @param port
	 * @return the key associated with the specified host and port, or null if it
	 *         doesn't exist.
	 */
	public static SKey getAquiredKey(String host, int port) {
		return store.get(toStoreName(host, port));
	}

	protected static SKey aquireNewShttpKey(SSLAuth auth, String host, int port, Logger log) {
		// Create send file
		HttpHeader header = new HttpHeader();

		header.setDescriptionLine(SHttp.makeInitRequestLine(CIPHER_KEY_SIZE));

		header.setAttribute("From", WSE.getApplicationName());
		header.setAttribute("User-Agent", "WebServiceEngine/" + WSE.VERSION);
		header.setAttribute("Host", host);
		header.setAttribute("Connection", "close");
		header.setContentLength(0);

		header.setSendContentLength(true);

		// Create connection
		HttpConnection connection = new HttpConnection(auth, host, port);
		connection.setUseSSL(true);

		log.finer("SHTTP init target: https://" + host + ":" + port);

		// Connect

		connection.connect(log);

		OutputStream output = connection.getOutputStream();

		// Print
		try {
			header.writeToStream(output, UTF8);
			output.flush();
		} catch (IOException e) {
			throw new WseConnectionException(e);
		}

		if (log.isLoggable(Level.FINE))
			log.fine("sHttp Init Request:\n" + header.toString());

		// Retrieve
		connection.read();

		HttpResult answer = connection.getRecievedHttp();

		if (log.isLoggable(Level.FINE))
			log.fine("sHttp Init Response Header:\n" + answer.getHeader().toPrettyString());

		HttpHeader responseHeader = answer.getHeader();

		if (responseHeader == null)
			throw new WseSHttpInitException("Failed to parse response header");

		HttpStatusLine status = responseHeader.getStatusLine();

		if (status == null)
			throw new WseSHttpInitException("Invalid response header: " + responseHeader.getDescriptionLine());

		// Check status code

		if (!status.isSuccessCode())
			throw new WseSHttpException("Failed to init shttp session. Got status \"" + status.toString());

		/**
		 * - key name - encryption key, base64 encoded - port number for shttp
		 * communication (since shttp is not ssl/tls based) - ttl in seconds
		 * 
		 */
		String payload;
		try {
			payload = new String(StreamUtils.readAll(answer.getContent()));
		} catch (IOException e) {
			throw new WseSHttpException("Failed to read shttp response: " + e.getMessage(), e);
		}
		String[] parts = payload.split(" ", 5);

		int sPort = 0;
		long exp = 0;
		int len = 0;
		try {
			sPort = Integer.parseInt(parts[2]);
			exp = Long.parseLong(parts[3]);
			len = Integer.parseInt(parts[4]) / 8;
		} catch (NumberFormatException e) {
			throw new WseSHttpException("Failed to parse shttp response: " + e.getMessage(), e);
		}

		String key_64 = parts[1];

		byte[] key = StringUtils.parseBase64Binary(key_64);

		byte[] finalKey = new byte[len];
		System.arraycopy(key, 0, finalKey, 0, finalKey.length);
		SKey skey = new SKey(parts[0], finalKey, host, port, sPort, exp);

		store(host, port, skey);

		return skey;
	}

}
