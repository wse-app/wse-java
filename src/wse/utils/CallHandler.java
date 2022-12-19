package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.client.IOConnection;
import wse.client.PersistantConnection;
import wse.client.PersistantConnectionStore;
import wse.client.SocketConnection;
import wse.client.WrappedConnection;
import wse.client.shttp.SHttpClientSessionStore;
import wse.utils.exception.SHttpException;
import wse.utils.exception.SecurityRetry;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseConnectionException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseHttpParsingException;
import wse.utils.exception.WseHttpStatusCodeException;
import wse.utils.exception.WseParsingException;
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
import wse.utils.stream.CountingInputStream;
import wse.utils.stream.LayeredOutputStream;
import wse.utils.stream.LimitedInputStream;
import wse.utils.stream.ProtectedOutputStream;
import wse.utils.stream.RecordingOutputStream;
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


	private final Timer timer;

	/** Logging */
	private Logger logger = WSE.getLogger();

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
	private HttpResult readResult;
	private HttpResult responseSHttp;
	protected HttpResult responseHttp;
	private HttpResult callResult;

	private OutputStream output;

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

		this.timer = new Timer(logger, Level.FINER);
	}
	
	public void setLogger(Logger logger) {
		this.logger = Objects.requireNonNull(logger);
		this.timer.setLogger(logger);
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

	/**
	 * Call the requested uri, response is never null, an exception is thrown
	 * instead
	 */
	public HttpResult call() throws WseException {
		try {
			initialize();
			for (int i = 0;; i++) {
				connect();
				try {
					write();
				} catch (WseException we) {
					if (we.getRootCause() instanceof SocketException) {
						if (connection instanceof PersistantConnection)
							continue;
					}
					throw we;
				}
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

			this.callResult = new HttpResult(responseHttp.getHeader(),
					new ConnectionClosingInputStream(responseHttp.getContent(), connection), false);
			return this.callResult;
		} catch (SoapFault sf) {
			throw sf;
		} catch (Exception e) {
			throw new WseException("Call to " + String.valueOf(withHiddenPassword(uri)) + " failed: " + e.getMessage(),
					e);
		}
	}

	private void initialize() {
		logger.fine("Target: " + String.valueOf(withHiddenPassword(uri)));

		options.log(logger, Level.FINER, "Options");

		protocol = Protocol.forName(uri.getScheme());
		if (protocol == null) {
			throw new WseException("Unknown protocol: " + uri.getScheme());
		}
		logger.finer("Protocol: " + protocol);

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

		logger.finest("Fetching connection");
		this.connection = getConnection();
		logger.finest("Got connection: " + this.connection.getClass().getName());

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
			logger.severe("Failed to connect: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new WseConnectionException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	private IOConnection getConnection() {
		IOConnection connection = getOptions().get(CallHandler.CONNECTION_OVERRIDE);
		if (connection != null)
			return connection;

		if (getAllowPersistentConnection()) {
			connection = PersistantConnectionStore.useConnection(protocol, host, usePort);

			if (connection != null) {
				// If we did not read all input from previous call, we might want to clear
				// the input before making another request
				try {
					InputStream input = connection.getInputStream();
					if (input.available() == 0) {
						logger.fine("Using persistant connection");
						return connection;
					}

					logger.finer("Persistant connection available but it has leftover data in input, ignored.");
				} catch (Throwable ignore) {
				}
			}
		}

		boolean ssl = protocol == Protocol.HTTPS || protocol == Protocol.WEB_SOCKET_SECURE;
		SocketConnection socketConnection = new SocketConnection(auth, ssl, host, usePort);
		socketConnection.setOptions(this);
		return socketConnection;
	}

	public IOConnection getWrappedConnection() throws IOException {
		if (this.connection == null)
			return null;
		return new WrappedConnection(this.connection, this.output);
	}

	private void shttpSetup() {

		timer.begin("getSHttpKey");
		skey = SHttpClientSessionStore.getKey(auth, host, port, logger);
		timer.end();

		if (skey == null) {
			throw new SHttpException("sHttp Setup failed: got null key");
		}

		this.usePort = skey.getReachPort();

		logger.fine("New target: " + this.host + ":" + usePort);
	}

	private void write() {
		timer.begin("Write");
		try {

			buildHttpHeader(httpHeader = new HttpHeader());
			boolean logContent = logContent(httpHeader.getContentType());

			if (this.protocol == Protocol.SHTTP) {

				LayeredOutputStream output = new LayeredOutputStream(
						new ProtectedOutputStream(connection.getOutputStream()));

				StreamCatcher shttpContent = new StreamCatcher();

				LayeredOutputStream shttpLayeredContent = new LayeredOutputStream(shttpContent);

				shttpLayeredContent.record(logger, Level.FINEST, "Request:");
				shttpLayeredContent.sHttpEncrypt(skey);

				httpHeader.writeToStream(shttpLayeredContent, StandardCharsets.UTF_8);

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
					output.record(logger, Level.FINEST, "SHttp-Encrypted Request:", true);

				shttpHeader.writeToStream(output, StandardCharsets.UTF_8);
				shttpContent.writeToStream(output, StandardCharsets.UTF_8);
				output.flush();

				this.output = output;

			} else {

				LayeredOutputStream output = new LayeredOutputStream(
						new ProtectedOutputStream(connection.getOutputStream()));

				if (logContent) {
					output.record(logger, Level.FINEST, "Request: ");
					httpHeader.writeToStream(output, StandardCharsets.UTF_8);
				} else {
					RecordingOutputStream ros = new RecordingOutputStream(output, logger, Level.FINEST,
							"Request Header: ");
					httpHeader.writeToStream(ros, StandardCharsets.UTF_8);
					ros.flush();
				}

				if (sendChunked)
					output.addChunked(8192);

				if (writer != null) {
					Charset cs = httpHeader.getContentCharset();
					if (cs == null)
						cs = StandardCharsets.UTF_8;
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
			InputStream inputStream = this.connection.getInputStream();
			this.readResult = HttpUtils.read(new CountingInputStream(inputStream, logger), true);
		} catch (IOException e) {
			throw new WseParsingException(e.getMessage(), e);
		}

		timer.end();

		if (protocol == Protocol.SHTTP && this.readResult.getHeader() != null) {
			responseSHttp = this.readResult;

			logger.finest("SHttp Response Header:\n" + responseSHttp.getHeader().toPrettyString());

			if (SHttp.SECURE_HTTP14.equals(this.readResult.getHeader().getStatusLine().getHttpVersion())) {
				InputStream httpMessage;
				try {
					httpMessage = SHttp.sHttpDecryptData(responseSHttp.getContent(), skey);
				} catch (Exception e) {
					throw new SHttpException("Failed to decrypt data: " + e.getMessage(), e);
				} finally {
					logger.finest("SHttp InputStream Image:\n" + String.valueOf(responseSHttp.getContent()));
				}

				try {
					this.responseHttp = HttpBuilder.read(httpMessage, true);
				} catch (IOException e) {
					throw new SHttpException("Failed to read http: " + e.getMessage(), e);
				}
			} else {
				logger.fine("SHTTP response version was not " + SHttp.SECURE_HTTP14);
				responseHttp = responseSHttp;
			}
		} else {
			responseHttp = this.readResult;
		}

		boolean persistant = getAllowPersistentConnection();
		persistant &= responseHttp.getHeader().getConnection(true).contains("keep-alive");

		if (persistant) {
			try {
				storePersistant();
			} catch (IOException ignore) {
			}
		}

		if (logger.isLoggable(Level.FINE)) {
			HttpHeader header = responseHttp.getHeader();
			if (header != null) {
				logger.fine("Response Header:\n" + header.toPrettyString());
			} else {
				logger.fine("Response Header: null");
			}
		}

		if (logger.isLoggable(Level.FINEST)) {
			if (!protocol.isWebSocket()) {
				responseHttp.wrapLogger("Response: ", logger, Level.FINEST);
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
			logger.fine("Storing persistant connection for: " + protocol + "://" + host + ":" + port);
			PersistantConnectionStore.storeConnection(protocol, host, port, null, null, connection);
			return;
		}

		System.out.println("GOT CONTENT");
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

			logger.fine("Upgrading to websocket v13");

		} else if (!line.isSuccessCode()) {

			if (line.getStatusCode() == 420) {
				SHttpClientSessionStore.invalidate(skey);
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
			if (logger.isLoggable(Level.SEVERE)) {
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

	private boolean logContent(ContentType ct) {
		if (ct == null)
			return false;
		MimeType mt = ct.parseType();
		if (mt == null)
			return false;
		if (mt.isText() || mt == MimeType.application.xml || mt == MimeType.application.json)
			return true;
		return false;
	}

	public void buildHttpHeader(HttpHeader header) {
		header.setDescriptionLine(new HttpRequestLine(method, HttpURI.fromURI(uri)));

		header.setFrom(WSE.getApplicationName());
		header.setUserAgent("WebServiceEngine/" + WSE.VERSION);
		header.setAcceptEncoding(TransferEncoding.IDENTITY);
		header.setHost(host);

		if (uri.getUserInfo() != null) {

			if (!this.protocol.isSecure()) {
				logger.warning("Sending credentials in non-secure http is prohibited");
			}
			header.setAttribute(HttpUtils.AUTHORIZATION,
					"Basic " + WSE.printBase64Binary(uri.getUserInfo().getBytes()));
		}

		if (getAllowPersistentConnection()) {
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
			cs = StandardCharsets.UTF_8;

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

	/**
	 * Retrieves the OutputStream associated with this CallHandler. The output
	 * stream
	 */
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

	/**
	 * Checks whether the current connection should be restored as a persistent connection.
	 * @return
	 */
	private boolean getAllowPersistentConnection() {
		
		// Disabled?
		if (!getOptions().get(CallHandler.PERSISTANT_CONNECTION, true))
			return false;

		// Not supported?
		if (!protocol.supportPersistentConnection()) 
			return false;
		
		return true;
	}

	public HttpResult getCallResult() {
		return callResult;
	}

	/**
	 * Help class to store persistant connection on
	 *
	 */
	public class PersistantInputStream extends WseInputStream {

		public PersistantInputStream(InputStream readFrom) {
			super(readFrom);
		}

		boolean stored = false;

		private void store() throws IOException {
			System.out.println("CallHandler.PersistantInputStream.store()");
			if (stored)
				return;
			stored = true;

			System.out.println("CLEANING INPUT");
			StreamUtils.clean(readFrom);
			logger.fine("Storing persistant connection for: " + protocol + "://" + host + ":" + port);
			PersistantConnectionStore.storeConnection(protocol, host, port, null, null, connection);
		}

		@Override
		public void close() throws IOException {
			System.out.println("CallHandler.PersistantInputStream.close()");
			new Exception().printStackTrace(System.out);
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
