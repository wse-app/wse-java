package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.client.IOConnection;
import wse.client.PersistantConnectionStore;
import wse.client.SocketConnection;
import wse.client.shttp.SHttpManager;
import wse.server.servlet.ws.WebSocketServlet;
import wse.utils.exception.SecurityRetry;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseConnectionException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseHttpParsingException;
import wse.utils.exception.WseHttpStatusCodeException;
import wse.utils.exception.WseParsingException;
import wse.utils.exception.SHttpException;
import wse.utils.http.ContentType;
import wse.utils.http.HttpAttributeList;
import wse.utils.http.HttpBuilder;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.http.HttpRequestLine;
import wse.utils.http.HttpStatusLine;
import wse.utils.http.HttpURI;
import wse.utils.http.StreamUtils;
import wse.utils.http.TransferEncoding;
import wse.utils.options.HasOptions;
import wse.utils.options.IOptions;
import wse.utils.options.Option;
import wse.utils.options.Options;
import wse.utils.shttp.SKey;
import wse.utils.ssl.SSLAuth;
import wse.utils.stream.LayeredOutputStream;
import wse.utils.stream.LimitedInputStream;
import wse.utils.stream.ProtectedOutputStream;
import wse.utils.stream.WseInputStream;
import wse.utils.writable.StreamCatcher;
import wse.utils.writer.HttpWriter;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class CallHandler implements HasOptions {

	/**
	 * Option for allowing the use of stored connections, and storing new ones for
	 * future connections to the same protocol/host/port
	 */
	public static final Option<Boolean> PERSISTANT_CONNECTION = new Option<>(CallHandler.class, "PERSISTANT_CONNECTION",
			true);
	public static final Option<IOConnection> CONNECTION_OVERRIDE = new Option<>(CallHandler.class,
			"CONNECTION_OVERRIDE");
	public static final Option<HttpAttributeList> ADDITIONAL_ATTRIBUTES = new Option<>(CallHandler.class,
			"ADDITIONAL_ATTRIBUTES");

	private static final Logger LOG = WSE.getLogger();
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final Timer timer;

	/** Call Info */
	private String host;
	private int port; // Port defined by the service
	private int usePort; // Port recieved by shttp if using shttp
	private Protocol protocol;

	private HttpMethod method;
	private URI uri;
	private SKey skey;
	private SSLAuth auth;

	/** Request */
	private HttpHeader httpHeader;
	private HttpHeader shttpHeader;
	private HttpWriter writer;

	/** Connection */
	private IOConnection connection;

	/** Response */
	protected HttpResult responseHttp;
	private HttpResult responseSHttp;

	private OutputStream output;
	private HttpResult result;

	private String webSocketKey;

	private boolean sendChunked = false;

	private final Options options = new Options();

	public CallHandler(HttpMethod method, URI uri, HttpWriter writer) {
		this(method, uri, writer, null);
	}

	public CallHandler(HttpMethod method, URI uri, HttpWriter writer, SSLAuth auth) {
		this.method = method;
		this.uri = uri;
		this.writer = writer;
		this.auth = auth;

		this.timer = new Timer(LOG, Level.FINER);
	}

	public IOptions getOptions() {
		return options;
	}

	@Override
	public void setOptions(HasOptions other) {
		options.setOptions(other);
	}

	public void setDataWriter(HttpWriter writer) {
		this.writer = writer;
	}

	public void setWebSocketControlKey(String key) {
		this.webSocketKey = key;
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

			return new HttpResult(responseHttp.getHeader(),
					new ConnectionClosingInputStream(responseHttp.getContent(), connection), false);
		} catch (SoapFault sf) {
			throw sf;
		} catch (Exception e) {
			throw new WseException("Call to " + String.valueOf(withHiddenPassword(uri)) + " failed: " + e.getMessage(),
					e);
		}
	}

	private void initialize() {
		LOG.fine("Target: " + String.valueOf(withHiddenPassword(uri)));

		options.log(LOG, Level.FINER, "Options");

		protocol = Protocol.forName(uri.getScheme());
		if (protocol == null) {
			throw new WseException("Unknown protocol: " + uri.getScheme());
		}
		LOG.finer("Protocol: " + protocol);

		host = uri.getHost();

		// Only care about host if default connection
		if (host == null && this.getOptions().get(CallHandler.CONNECTION_OVERRIDE) == null) {
			throw new WseException("Got host == null");
		}

		this.port = uri.getPort();
		if (port == -1) {
			if (protocol.isSecure())
				port = 443;
			else
				port = 80;
		}

	}

	private void connect() {

		if (protocol == Protocol.SHTTP) {
			shttpSetup();
		} else {
			usePort = port;
		}

		this.connection = getConnection();

		try {
			if (this.connection.isOpen()) {
				return;
			}
		} catch (IOException ignore) {
		}

		try {
			timer.begin("Connect");
			connection.connect();
			timer.end();
		} catch (Exception e) {
			LOG.severe("Failed to connect: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new WseConnectionException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	private IOConnection getConnection() {

		IOConnection connection = getOptions().get(CallHandler.CONNECTION_OVERRIDE);
		if (connection != null)
			return connection;

		if (persistant()) {
			connection = PersistantConnectionStore.useConnection(protocol, host, usePort);

			if (connection != null) {
				// TODO If we did not read all input from previous call, we might want to clear
				// the input before making another request
				try {
					InputStream input = connection.getInputStream();
					if (input.available() == 0) {
						LOG.fine("Using persistant connection");
						return connection;
					}

					LOG.warning("Persistant connection available but it has leftover data in input");
				} catch (Throwable ignore) {
				}
			}
		}

		boolean ssl = protocol == Protocol.HTTPS || protocol == Protocol.WEB_SOCKET_SECURE;
		SocketConnection socketConnection = new SocketConnection(auth, ssl, host, usePort);
		socketConnection.setOptions(this);
		return socketConnection;
	}

	private void shttpSetup() {

		timer.begin("getSHttpKey()");
		skey = SHttpManager.getKey(auth, host, port, LOG);
		timer.end();

		if (skey == null) {
			throw new SHttpException("sHttp Setup failed: got null key");
		}

		this.usePort = skey.getReachPort();

		LOG.fine("New target: " + this.host + ":" + usePort);
	}

	private void write() {
		timer.begin("Write");
		try {
			if (this.protocol == Protocol.SHTTP) {

				LayeredOutputStream output = new LayeredOutputStream(
						new ProtectedOutputStream(connection.getOutputStream()));

				StreamCatcher shttpContent = new StreamCatcher();

				LayeredOutputStream shttpLayeredContent = new LayeredOutputStream(shttpContent);

				shttpLayeredContent.record(LOG, Level.FINEST, "Request:");
				shttpLayeredContent.sHttpEncrypt(skey);

				buildHttpHeader(httpHeader = new HttpHeader());

				httpHeader.writeToStream(shttpLayeredContent, UTF8);

				if (sendChunked)
					shttpLayeredContent.addChunked(8192);

				if (writer != null) {
					Charset cs = httpHeader.getContentCharset();
					writer.writeToStream(shttpLayeredContent, cs);
				}

				shttpLayeredContent.flush();
				shttpContent.flush();

				shttpHeader = SHttp.makeSHttpHeader(skey.getKeyName());
				shttpHeader.setContentLength(shttpContent.getSize());

				if (SHttp.LOG_ENCRYPTED_DATA)
					output.record(LOG, Level.FINEST, "SHttp-Encrypted Request:", true);

				shttpHeader.writeToStream(output, UTF8);
				shttpContent.writeToStream(output, UTF8);
				output.flush();

				this.output = output;

			} else {

				LayeredOutputStream output = new LayeredOutputStream(
						new ProtectedOutputStream(connection.getOutputStream()));

				output.record(LOG, Level.FINEST, "Request: ");
				buildHttpHeader(httpHeader = new HttpHeader());
				httpHeader.writeToStream(output, UTF8);

				if (sendChunked)
					output.addChunked(8192);

				if (writer != null) {
					Charset cs = httpHeader.getContentCharset();
					if (cs == null)
						cs = UTF8;
					writer.writeToStream(output, cs);
				}
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

		// Never null
		try {
			this.result = HttpUtils.read(this.connection, true);
		} catch (IOException e) {
			throw new WseParsingException(e.getMessage(), e);
		}

		timer.end();

		if (protocol == Protocol.SHTTP && this.result.getHeader() != null) {
			responseSHttp = this.result;

			LOG.finest("SHttp Response Header:\n" + responseSHttp.getHeader().toPrettyString());

			if (SHttp.SECURE_HTTP14.equals(this.result.getHeader().getStatusLine().getHttpVersion())) {
				InputStream httpMessage;
				try {
					httpMessage = SHttp.sHttpDecryptData(responseSHttp.getContent(), skey);
				} catch (Exception e) {
					throw new SHttpException("Failed to decrypt data: " + e.getMessage(), e);
				} finally {
					LOG.finest("SHttp InputStream Image:\n" + String.valueOf(responseSHttp.getContent()));
				}

				try {
					this.responseHttp = HttpBuilder.read(httpMessage, true);
				} catch (IOException e) {
					throw new SHttpException("Failed to read http: " + e.getMessage(), e);
				}
			} else {
				LOG.fine("SHTTP response was not " + SHttp.SECURE_HTTP14);
				responseHttp = responseSHttp;
			}
		} else {
			responseHttp = this.result;
		}

		boolean persistant = persistant();
		persistant &= responseHttp.getHeader().getConnection(true).contains("keep-alive");

		if (persistant) {
			try {
				storePersistant();
			} catch (IOException ignore) {
			}
		}

		if (LOG.isLoggable(Level.FINE)) {
			HttpHeader header = responseHttp.getHeader();
			if (header != null) {
				LOG.fine("Response Header:\n" + header.toPrettyString());
			} else {
				LOG.fine("Response Header: null");
			}
		}

		if (LOG.isLoggable(Level.FINEST)) {
			if (!protocol.isWebSocket()) {
				responseHttp.wrapLogger("Response: ", LOG, Level.FINEST);
			}
		}
	}

	private void storePersistant() throws IOException {
		boolean hasContent = false;

		hasContent = responseHttp.getHeader().getContentLength() > 0;

		if (!hasContent) {
			TransferEncoding encoding = responseHttp.getHeader().getTransferEncoding();
			hasContent = encoding == TransferEncoding.CHUNKED;
		}
		if (!hasContent) {
			InputStream is = responseHttp.getContent();
			hasContent = is.available() > 0;
		}

		if (!hasContent) {
			LOG.fine("Storing persistant connection for: " + protocol + "://" + host + ":" + port);
			PersistantConnectionStore.storeConnection(protocol, host, port, null, null, connection);
			return;
		}

		responseHttp.setContent(new PersistantInputStream(responseHttp.getContent()));
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

		if ((this.protocol.isWebSocket() && line.getStatusCode() == HttpCodes.SWITCHING_PROTOCOLS)) {

			LOG.fine("Upgrading to websocket v13");

		} else if (!line.isSuccessCode()) {

			if (line.getStatusCode() == 420) {
				SHttpManager.invalidate(skey);
				throw new SecurityRetry();
			}

			ContentType ct = header.getContentType();
			MimeType mt = header.getContentType().parseType();

			boolean isXML = mt != null && mt == MimeType.text.xml || mt == MimeType.application.xml;

			if (isXML) {
				// Will throw SoapFault exception
				soapFault(ct);

				// Could not parse as soapFault, fall through
			}

			String errMsg = null;
			if (LOG.isLoggable(Level.SEVERE)) {
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
	}

	private void soapFault(ContentType ct) {

		SoapFault fault = null;

		try {
			Charset cs = ct.getCharsetParsed();
			if (cs == null)
				cs = Charset.forName("UTF-8");

			XMLElement env = XMLUtils.XML_PARSER.parse(responseHttp.getContent(), cs);

			XMLElement bodyXml = env.getChild("Body", XMLUtils.SOAP_ENVELOPE);
			XMLElement faultXml = bodyXml.getChild("Fault");

			fault = new SoapFault(faultXml);
		} catch (Throwable e) {
			return;
		}

		throw fault;
	}

	public void buildHttpHeader(HttpHeader header) {
		header.setDescriptionLine(new HttpRequestLine(method, HttpURI.fromURI(uri)));

		header.setFrom(WSE.getApplicationName());
		header.setUserAgent("WebServiceEngine/" + WSE.VERSION);
		header.setAcceptEncoding(TransferEncoding.IDENTITY);
		header.setHost(host, port, protocol);

		if (uri.getUserInfo() != null) {

			if (!this.protocol.isSecure()) {
				LOG.warning("Sending credentials in non-secure http is prohibited");
			}
			header.setAttribute(HttpUtils.AUTHORIZATION,
					"Basic " + WSE.printBase64Binary(uri.getUserInfo().getBytes()));
		}

		if (this.protocol.isWebSocket()) {
			header.setAttribute(WebSocketServlet.ATTRIB_UPGRADE, WebSocketServlet.UPGRADE_VALUE);
			header.setAttribute(WebSocketServlet.ATTRIB_CONNECTION, WebSocketServlet.CONNECTION_VALUE);
			header.setAttribute(WebSocketServlet.ATTRIB_KEY, this.webSocketKey);
			header.setAttribute(WebSocketServlet.ATTRIB_VERSION, "13");
		}

		if (persistant()) {
			header.setConnection("keep-alive");
		} else {
			header.setConnection("close");
		}

		HttpAttributeList additional = getOptions().get(ADDITIONAL_ATTRIBUTES);
		if (additional != null) {
			header.putAll(additional);
		}

		if (writer != null)
			writer.prepareHeader(header);

		Charset cs = header.getContentCharset();
		if (cs == null)
			cs = UTF8;

		TransferEncoding enc = header.getTransferEncoding();
		if (enc == null)
			enc = TransferEncoding.IDENTITY;

		switch (enc) {
		case IDENTITY:
			long l = writer != null ? writer.requestContentLength(cs) : (this.protocol.isWebSocket() ? -1 : 0);
			header.setSendContentLength(l >= 0);
			if (l >= 0)
				header.setContentLength(l);
			break;
		case CHUNKED:
			this.sendChunked = true;
			header.setSendContentLength(false);
			break;
		default:
			throw new WseException("Transfer-Encoding '" + enc.name + "' not supported");
		}
	}

	public OutputStream getOutput() {
		return output;
	}

	protected static class ConnectionClosingInputStream extends WseInputStream {

		private final IOConnection close;

		public ConnectionClosingInputStream(InputStream stream, IOConnection close) {
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

	private boolean persistant() {
		boolean persistant = getOptions().get(CallHandler.PERSISTANT_CONNECTION, true);
		return persistant && (protocol == Protocol.HTTPS || protocol == Protocol.HTTP);
	}

	public class PersistantInputStream extends WseInputStream {

		public PersistantInputStream(InputStream readFrom) {
			super(readFrom);
		}

		boolean stored = false;

		private void store() throws IOException {
			if (stored)
				return;
			stored = true;

			LOG.fine("Storing persistant connection for: " + protocol + "://" + host + ":" + port);
			StreamUtils.clean(readFrom);
			PersistantConnectionStore.storeConnection(protocol, host, port, null, null, connection);
		}

		@Override
		public void close() throws IOException {
			store();
		}

		@Override
		public int read() throws IOException {
			int i = super.read();
			if (i == -1)
				store();
			return i;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int i = super.read(b, off, len);
			if (i == -1)
				store();
			return i;
		}

	}
}
