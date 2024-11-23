package wse.utils.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.servlet.ws.PongListener;
import wse.server.servlet.ws.WebSocketServlet;
import wse.utils.CallHandler;
import wse.utils.HttpResult;
import wse.utils.StringUtils;
import wse.utils.Suppliers;
import wse.utils.event.ListenerRegistration;
import wse.utils.exception.WebSocketException;
import wse.utils.exception.WebSocketHandshakeException;
import wse.utils.http.HeaderAttribute;
import wse.utils.http.HttpAttributeList;
import wse.utils.http.HttpMethod;
import wse.utils.options.HasOptions;
import wse.utils.options.IOptions;
import wse.utils.options.Options;
import wse.utils.ssl.SSLAuth;
import wse.utils.writable.StreamWriter;

/**
 * @author WSE
 */
public class WebSocket implements WebSocketCodes, HasOptions {

	public static String LOG_CHILD_NAME = "WebSocket";

	private final Logger log = WSE.getLogger(LOG_CHILD_NAME);

	public static String getCodeName(int code) {
		switch (code) {
		case OP_CONTINUE:
			return "Continue";
		case OP_TEXT:
			return "Text";
		case OP_BINARY:
			return "Binary";
		case OP_CLOSE:
			return "Close";
		case OP_PING:
			return "Ping";
		case OP_PONG:
			return "Pong";
		}
		return "Invalid code: " + code;
	}

	public static String getWebSocketAcceptFromKey(String key) {
		try {
			return StringUtils.printBase64Binary(MessageDigest.getInstance("SHA-1")
					.digest((key + WebSocket.WEB_SOCKET_MAGIC_STR).getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private final WebSocketEndpointImpl endpoint;
	private final Options options = new Options();
	private final URI uri;
	private final SSLAuth auth;

	private boolean isOpening;
	private boolean isOpened;

	public WebSocket(URI uri, WebSocketListener listener) {
		this(uri, null, listener);
	}

	public WebSocket(URI uri, SSLAuth auth, WebSocketListener listener) {
		this.uri = uri;
		this.auth = auth;
		this.endpoint = new WebSocketEndpointImpl(true);
		this.registerListener(listener);

		if (this.handler != null) {
			throw new WebSocketException("WebSocket has already been opened");
		}
	}

	private CallHandler handler;
	private HttpResult handshakeResult;

	private InputStream input;
	private OutputStream output;

	private Thread thread;
	public static final String WEB_SOCKET_MAGIC_STR = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

	public void openAsync() {
		isOpeningCheck();
		
		thread = new Thread(new Runnable() {
			public void run() {
				try {
					openInternal();
				} catch (Exception e) {
					e.printStackTrace();
					log.severe("Failed to open websocket: " + e.getMessage());
				}
			}
		});
		thread.start();
	}

	public void open() throws IOException {
		isOpeningCheck();
		openInternal();
	}

	private void openInternal() throws IOException {
		try {
			doHandshake();
		} catch (Exception e) {
			throw new WebSocketException("Error during WebSocket handshake: " + e.getMessage(), e);
		}

		this.isOpened = true;

		this.input = handshakeResult.getContent();
		this.output = handler.getOutput();
		this.endpoint.setInputStream(this.input);
		this.endpoint.setOutputStream(this.output);

		this.endpoint.onInit(this.handshakeResult.getHeader());

		try {
			endpoint.readLoop();
		} catch (Exception e) {
			forceClose(e.getMessage(), e);
		}
	}

	private void isOpeningCheck() {
		if (isOpening)
			throw new IllegalStateException("This socket has already been opened");
		synchronized (this) {
			if (isOpening)
				throw new IllegalStateException("This socket has already been opened");
			isOpening = true;
		}
	}

	private void doHandshake() throws IOException {
		this.handler = new CallHandler(HttpMethod.GET, this.uri, null, this.auth);
		this.handler.setLogger(WSE.getLogger("WebSocket"));

		String controlKey = controlKey();

		HttpAttributeList additional = options.get(CallHandler.ADDITIONAL_ATTRIBUTES,
				Suppliers.ofClass(HttpAttributeList.class));

		additional.setAttribute(WebSocketServlet.ATTRIB_UPGRADE, WebSocketServlet.UPGRADE_VALUE);
		additional.setAttribute(WebSocketServlet.ATTRIB_CONNECTION, WebSocketServlet.CONNECTION_VALUE);
		additional.setAttribute(WebSocketServlet.ATTRIB_KEY, controlKey);
		additional.setAttribute(WebSocketServlet.ATTRIB_VERSION, "13");

		options.set(CallHandler.ADDITIONAL_ATTRIBUTES, additional);
		this.handler.setOptions(this);

		String expectedResponseKey = getWebSocketAcceptFromKey(controlKey);

		try {
			this.handshakeResult = handler.call();
		} catch (Throwable t) {
			throw new WebSocketHandshakeException(t.getMessage(), t);
		}

		HeaderAttribute acc = this.handshakeResult.getHeader().getAttribute("Sec-WebSocket-Accept");
		if (acc == null || !acc.hasValue() || !Objects.equals(expectedResponseKey, acc.value)) {
			throw new WebSocketHandshakeException("Server responded with invalid accept key");
		}

		// notifications.js:28 WebSocket connection to 'ws://localhost:8505/chat'
		// failed: Error during WebSocket handshake: 'Connection' header value must
		// contain 'Upgrade'

		HeaderAttribute connection = this.handshakeResult.getHeader().getAttribute("Connection");
		if (connection == null || !connection.hasValue() || !"Upgrade".equals(connection.value))
			throw new WebSocketHandshakeException("'Connection' response header value must contain 'Upgrade'");

		HeaderAttribute upgrade = this.handshakeResult.getHeader().getAttribute("Upgrade");
		if (upgrade == null || !upgrade.hasValue() || !"websocket".equals(upgrade.value))
			throw new WebSocketHandshakeException("'Upgrade' response header value must contain 'websocket'");
	}

	private String controlKey() {
		byte[] key = new byte[8];
		new Random().nextBytes(key);
		return WSE.printBase64Binary(key);
	}

	public void sendMessage(byte opcode, StreamWriter writer) throws IOException {
		endpoint.sendMessage(opcode, writer);
	}

	public void pingAsync(PongListener listener) throws IOException {
		endpoint.pingAsync(listener);
	}

	public void close(String shutDownMessage) throws IOException {
		endpoint.close(shutDownMessage);
	}

	public void forceClose(String err, Throwable e) {
		log.severe("WebSocket forcing close: " + (e != null ? e.getMessage() : err));
		try {
			this.input.close(); // The input is connected to socket
		} catch (Exception ignore) {
		}
		try {
			endpoint.forceClose(err);
		} catch (Exception e2) {
			log.severe(e2.getMessage());
		}
	}

	public ListenerRegistration registerListener(WebSocketListener listener) {
		return endpoint.registerListener(listener);
	}

	public void sendMessage(final String line) throws IOException {
		sendMessage(OP_TEXT, new StreamWriter() {
			@Override
			public void writeToStream(OutputStream output, Charset charset) throws IOException {
				output.write(line.getBytes(charset));
				output.flush();
			}
		});
	}

	@Override
	public IOptions getOptions() {
		return options;
	}

	public boolean isOpen() {
		if (!isOpening) 
			return false;
		if (isOpened)
			return endpoint.isOpen();
		return true;
		
	}

	@Override
	public void setOptions(HasOptions other) {
		options.setOptions(other);
	}
}
