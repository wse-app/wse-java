package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.client.HttpConnection;
import wse.client.shttp.SHttpManager;
import wse.server.servlet.ws.WebSocketServlet;
import wse.utils.exception.SecurityRetry;
import wse.utils.exception.WseConnectionException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseHttpParsingException;
import wse.utils.exception.WseHttpStatusCodeException;
import wse.utils.exception.WseInitException;
import wse.utils.exception.WseSHttpException;
import wse.utils.http.HttpBuilder;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.http.HttpRequestLine;
import wse.utils.http.HttpStatusLine;
import wse.utils.http.HttpURI;
import wse.utils.http.StreamUtils;
import wse.utils.http.TransferEncoding;
import wse.utils.log.Loggers;
import wse.utils.shttp.SKey;
import wse.utils.ssl.SSLAuth;
import wse.utils.stream.LayeredOutputStream;
import wse.utils.stream.LimitedInputStream;
import wse.utils.stream.ProtectedOutputStream;
import wse.utils.stream.WseInputStream;
import wse.utils.writable.StreamCatcher;

public class CallHandler {

	private static Logger log = WSE.getLogger();

	private CallTimer timer;

	/** Call Info */
	private String host;
	private int port; // Port defined by the service
	private int usePort; // Port recieved by shttp if using shttp
	private Protocol protocol;

	/** Request */
	private HttpHeader http_header;
	private HttpHeader shttp_header;

	/** Connection */
	private HttpConnection connection;

	/** Response */
	protected HttpResult responseHttp;
	private HttpResult responseSHttp;

	private HttpWriter writer;
	private URI uri;
	private HttpMethod method;
	private SSLAuth auth;
	private SKey skey;

	private OutputStream output;

	private HttpResult result;
	private String websocket_key;

	private int readTimeout = 10 * 1000;
	private boolean sendChunked = false;

	public CallHandler(HttpMethod method, URI uri, HttpWriter writer) {
		this(method, uri, writer, null);
	}

	public CallHandler(HttpMethod method, URI uri, HttpWriter writer, SSLAuth auth) {
		timer = new CallTimer(log);
		this.writer = writer;
		this.uri = uri;
		this.method = method;
		this.auth = auth;
	}

	public void setDataWriter(HttpWriter writer) {
		this.writer = writer;
	}

	public void setWebSocketControlKey(String key) {
		this.websocket_key = key;
	}

	public void setSoTimeout(int timeout) throws SocketException {
		this.readTimeout = timeout;
		if (this.connection != null) {
			this.connection.setSoTimeout(timeout);
		}
	}

	/**
	 * Call the requested uri, response is never null, an exception is thrown
	 * instead
	 */
	public HttpResult call() throws WseException {
		try {
			initialize();
			for (int i = 0; i < 2; i++) {
				connect();
				write();
				read();
				try {
					loadResponse();
				} catch (SecurityRetry e) {
					if (i == 0)
						continue;
					throw e;
				}
				break;
			}
			if (result == null) {
				throw new WseException("Got null result");
			}
			return new HttpResult(responseHttp.getHeader(),
					new ConnectionClosingInputStream(responseHttp.getContent(), connection), false);
		} catch (Exception e) {
			throw new WseException("Call to " + String.valueOf(withHiddenPassword(uri)) + " failed: " + e.getMessage(),
					e);
		}
	}

	private void initialize() {

		protocol = Protocol.parse(uri.getScheme());
		if (protocol == null) {
			throw new WseInitException("Got invalid protocol: " + uri.getScheme());
		}
		log.fine("Protocol: " + protocol);

		host = uri.getHost();

		if (host == null) {
			throw new WseInitException("Got host == null");
		}
		this.port = uri.getPort();
		if (port == -1) {
			if (protocol.isSecure())
				port = 443;
			else
				port = 80;
		}

		log.fine("Target: " + String.valueOf(withHiddenPassword(uri)));
	}

	private void connect() {

		if (protocol == Protocol.SHTTP) {
			Loggers.trace(log, "shttpSetup()", new Runnable() {
				@Override
				public void run() {
					shttpSetup();
				}
			});
		} else {
			usePort = port;
		}

		connection = new HttpConnection(auth, host, usePort);
		try {
			connection.setSoTimeout(this.readTimeout);
		} catch (SocketException e) {
			throw new WseConnectionException("Failed to set SoTimeout: " + e.getMessage(), e);
		}

		if (protocol == Protocol.HTTPS || protocol == Protocol.WEB_SOCKET_SECURE) {
			connection.setUseSSL(true);
		}

		timer.begin("Connect");
		connection.connect(log);
		timer.end();
	}

	private void shttpSetup() {

		timer.begin("getSHttpKey()");
		skey = SHttpManager.getKey(auth, host, port, log);
		timer.end();

		if (skey == null) {
			throw new WseSHttpException("sHttp Setup failed: got null key");
		}

		this.usePort = skey.getReachPort();

		log.fine("New target: " + this.host + ":" + usePort);
	}

	private void write() {
		timer.begin("Write");
		try {
			if (this.protocol == Protocol.SHTTP) {

				LayeredOutputStream output = new LayeredOutputStream(
						new ProtectedOutputStream(connection.getOutputStream()));

				StreamCatcher content = new StreamCatcher();

				LayeredOutputStream contentStream = new LayeredOutputStream(content);

				contentStream.record(log, Level.FINEST, "Request:");
				contentStream.sHttpEncrypt(skey);

				buildHttpHeader(http_header = new HttpHeader());
				http_header.writeToStream(contentStream);

				if (sendChunked)
					contentStream.addChunked(8192);

				if (writer != null)
					writer.writeToStream(contentStream);

				contentStream.flush();
				content.flush();

				shttp_header = SHttp.makeSHttpHeader(skey.getKeyName());
				shttp_header.setContentLength(content.getSize());

				if (SHttp.LOG_ENCRYPTED_DATA)
					output.record(log, Level.FINEST, "SHttp-Encrypted Request:", true);

				shttp_header.writeToStream(output);
				content.writeToStream(output);
				output.flush();

				this.output = output;

			} else {

				LayeredOutputStream output = new LayeredOutputStream(new ProtectedOutputStream(connection.getOutputStream()));

				output.record(log, Level.FINEST, "Request: ");
				buildHttpHeader(http_header = new HttpHeader());
				http_header.writeToStream(output);

				if (sendChunked)
					output.addChunked(8192);

				if (writer != null)
					writer.writeToStream(output);
				output.flush();

				this.output = output;
			}

		} catch (IOException e) {
			throw new WseConnectionException("Failed to write: " + e.getMessage(), e);
		} finally {
			timer.end();
		}
	}

	private void read() {

		timer.begin("Read");

		connection.read(true);

		this.result = connection.getRecievedHttp();

		timer.end();

		if (protocol == Protocol.SHTTP && this.result.getHeader() != null) {
			responseSHttp = this.result;

			log.finest("SHttp Response Header:\n" + responseSHttp.getHeader().toPrettyString());

			if (SHttp.SECURE_HTTP14.equals(this.result.getHeader().getStatusLine().getHttpVersion())) {
				InputStream httpMessage;
				try {
					httpMessage = SHttp.sHttpDecryptData(responseSHttp.getContent(), skey);
				} catch (Exception e) {
					throw new WseSHttpException("Failed to decrypt data: " + e.getMessage(), e);
				} finally {
					log.finest("InputStream Image:\n" + String.valueOf(responseSHttp.getContent()));
				}

				try {
					this.responseHttp = HttpBuilder.read(httpMessage, true);
				} catch (IOException e) {
					throw new WseSHttpException("Failed to read http: " + e.getMessage(), e);
				}
			} else {
				log.fine("SHTTP response was not " + SHttp.SECURE_HTTP14);
				responseHttp = responseSHttp;
			}
		} else {
			responseHttp = this.result;
		}

		if (log.isLoggable(Level.FINE)) {
			HttpHeader header = responseHttp.getHeader();
			if (header != null) {
				log.fine("Response Header:\n" + header.toPrettyString());
			} else {
				log.fine("Response Header: null");
			}
		}

		if (log.isLoggable(Level.FINEST)) {
			if (!protocol.isWebSocket()) {
				responseHttp.wrapLogger("Response: ", log, Level.FINEST);
			}
		}

	}

	private void loadResponse() {
		validateHeaderCode();
	}

	private void validateHeaderCode() {

		HttpHeader header = responseHttp.getHeader();
		if (header == null) {
			throw new WseHttpParsingException("Got null response header");
		}

		HttpStatusLine line = header.getStatusLine();

		if (line == null) {
			throw new WseHttpParsingException("Invalid response status line: " + header.getDescriptionLine());
		}

		if ((this.protocol.isWebSocket() && line.getStatusCode() == 101)) {

			log.fine("Upgrading to websocket v13");

		} else if (!line.isSuccessCode()) {

			if (line.getStatusCode() == 420) {
				SHttpManager.invalidate(skey);
				throw new SecurityRetry();
			}

			if (!header.getContentType().is(MimeType.text.xml)) {
				String errMsg = null;
				if (log.isLoggable(Level.SEVERE)) {
					if (responseHttp != null) {
						if (responseHttp.getContent() != null) {
							try {
								errMsg = new String(
										StreamUtils.readAll(new LimitedInputStream(responseHttp.getContent(), 2000)));
							} catch (IOException e) {
							}
						}
					}
				}

				throw new WseHttpStatusCodeException(
						"Got bad response status: \"" + line.toString().trim() + "\" errMsg: " + errMsg,
						line.getStatusCode());
			}

			throw new WseHttpStatusCodeException("Got bad response status: \"" + line.toString().trim() + "\"",
					line.getStatusCode());
		}
	}

	public void buildHttpHeader(HttpHeader header) {
		header.setDescriptionLine(new HttpRequestLine(method, HttpURI.fromURI(uri)));

		header.setFrom(WSE.getApplicationName());
		header.setUserAgent("WebServiceEngine/" + WSE.VERSION);
		header.setAcceptEncoding(TransferEncoding.IDENTITY);
		header.setHost(host, port, protocol);

		if (uri.getUserInfo() != null) {

			if (!this.protocol.isSecure()) {
				log.warning("Sending credentials in non-secure http is prohibited");
			}
			header.setAttribute(HttpUtils.AUTHORIZATION,
					"Basic " + WSE.printBase64Binary(uri.getUserInfo().getBytes()));
		}
		if (this.protocol.isWebSocket()) {

			header.setAttribute(WebSocketServlet.ATTRIB_UPGRADE, WebSocketServlet.UPGRADE_VALUE);
			header.setAttribute(WebSocketServlet.ATTRIB_CONNECTION, WebSocketServlet.CONNECTION_VALUE);
			header.setAttribute(WebSocketServlet.ATTRIB_KEY, this.websocket_key);
			header.setAttribute(WebSocketServlet.ATTRIB_VERSION, "13");
		} else {
			header.setAttribute(HttpUtils.CONNECTION, HttpUtils.CONNECTION_CLOSE);
		}

		if (writer != null)
			writer.prepareHeader(header);

		TransferEncoding enc = header.getTransferEncoding();
		if (enc == null)
			enc = TransferEncoding.IDENTITY;

		switch (enc) {
		case BR: // fall through
		case COMPRESS: // fall through
		case DEFLATE: // fall through
		case GZIP:
			throw new WseException("Transfer-Encoding '" + enc.name + "' not supported");
		case IDENTITY:
			long l = writer != null ? writer.requestContentLength() : (this.protocol.isWebSocket() ? -1 : 0);
			header.setSendContentLength(l >= 0);
			if (l >= 0)
				header.setContentLength(l);
			break;
		case CHUNKED:
			this.sendChunked = true;
			header.setSendContentLength(false);
			break;
		default:
			break;
		}
	}

	public OutputStream getOutput() {
		return output;
	}

	protected static class ConnectionClosingInputStream extends WseInputStream {

		private final HttpConnection close;

		public ConnectionClosingInputStream(InputStream stream, HttpConnection close) {
			super(stream);
			this.close = close;
		}

		@Override
		public void close() throws IOException {
			super.close();
			close.close();
		}

	}

	public static URI withHiddenPassword(URI uri) {
		if (uri == null)
			return null;
		try {
			String user = uri.getUserInfo();
			if (user != null) {
				String[] parts = user.split(":", 2);
				if (parts.length == 2) {
					user = parts[0] + ":" + StringUtils.stack("*", parts[1].length());
				}
			}

			return new URI(uri.getScheme(), user, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
					uri.getFragment());
		} catch (URISyntaxException e) {
			return null;
		}
	}
}