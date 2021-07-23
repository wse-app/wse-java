package wse.utils.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.servlet.ws.PongListener;
import wse.utils.CallHandler;
import wse.utils.HttpResult;
import wse.utils.StringUtils;
import wse.utils.event.ListenerRegistration;
import wse.utils.exception.WseWebSocketException;
import wse.utils.exception.WseWebSocketHandshakeException;
import wse.utils.http.HeaderAttribute;
import wse.utils.http.HttpMethod;
import wse.utils.ssl.SSLAuth;
import wse.utils.stream.CombinedInputStream;
import wse.utils.stream.WS13OutputStream;
import wse.utils.stream.XORInputStream;
import wse.utils.writable.StreamWriter;

/**
 * 
 * @author WSE
 *
 */
public class WebSocket implements WebSocketCodes {

	public static final String getCodeName(int code) {
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

	public static class Message {
		private List<Frame> frames = new ArrayList<>();

		public List<Frame> getFrames() {
			return frames;
		}

		public int getOPCode() {
			if (frames.size() <= 0)
				return -1;

			return frames.get(0).getOpcode();
		}

		public long getContentLength() {
			long res = 0;
			for (Frame f : frames)
				res += f.payload_length;
			return res;
		}

		public boolean isEveryFrameMasked() {
			for (Frame f : frames)
				if (!f.isMasked())
					return false;
			return true;
		}

		public InputStream inputStream() {
			InputStream[] streams = new InputStream[frames.size()];
			for (int i = 0; i < frames.size(); i++) {
				Frame f = frames.get(i);
				streams[i] = new ByteArrayInputStream(f.getPayload());
				if (f.isMasked() && !WS13OutputStream.isKeyIdentity(f.getKey())) {
					streams[i] = new XORInputStream(streams[i], f.getKey());
				}
			}
			return new CombinedInputStream(streams);
		}
	}

	public static class Frame {
		private boolean fin;
		private boolean masked;
		private int opcode;
		private int reserved;
		private byte[] key;

		private long payload_length;
		private byte[] payload;

		public boolean isFin() {
			return fin;
		}

		public boolean isMasked() {
			return masked;
		}

		public int getOpcode() {
			return opcode;
		}

		public byte[] getKey() {
			return key;
		}

		public byte[] getPayload() {
			return payload;
		}

		public boolean getRSV1() {
			return ((reserved & 0b100) >> 2) == 1;
		}

		public boolean getRSV2() {
			return ((reserved & 0b010) >> 1) == 1;
		}

		public boolean getRSV3() {
			return ((reserved & 0b001) >> 0) == 1;
		}

	}

	public static Frame readNextFrame(InputStream stream, boolean client) throws IOException {

		byte[] f2 = read(stream, 2);
		if (f2 == null) {
			throw new WseWebSocketException("got end of stream");
		}

		int first = f2[0];
		Frame result = new Frame();

		result.fin = ((first & FIN_MASK) >> 7) == 1; // bit 0 (1)
		result.reserved = ((first & RSV_MASK) >> 4) & 0b111; // bit 1-3 (3)
		result.opcode = first & OPCODE_MASK; // bit 4-7 (4)

//		System.out.println("First byte: " + Integer.toBinaryString(first));

		int second = f2[1];
		result.masked = ((second >> 7) & 0b1) == 1; // bit 8 (1)

		if (!client && !result.masked) {
			// Should abort socket: http://tools.ietf.org/html/rfc6455#section-5.1
			throw new WseWebSocketException("Server got unmasked websocket message");
		}

		// Read payload length
		int len_info = (second & LENGTH_MASK); // bit 9-15 (7)
		if (len_info <= 125) {
			result.payload_length = len_info;
		} else if (len_info == 126) {
			byte[] len = read(stream, 2);
			result.payload_length = ByteBuffer.wrap(len).getShort();
		} else { // len == 127
			byte[] len = read(stream, 8);
			result.payload_length = ByteBuffer.wrap(len).getLong();
		}

		// Read key if the payload is XOR masked
		if (result.masked) {
			result.key = read(stream, 4); // 32-bit key
		}

		result.payload = read(stream, (int) result.payload_length);
		return result;
	}

	/**
	 * Reads a specified number of bytes, will return null if end of stream is
	 * reached before every byte has been read.<br>
	 * Blocks until <b>length</b> number of bytes has been read or end of stream is
	 * reached.
	 */
	private static byte[] read(InputStream stream, int length) throws IOException {
		byte[] result = new byte[length];
		int a = 0, p = 0;
		while ((a = stream.read(result, p, length - p)) != -1) {
			p += a;
			if (p >= result.length)
				return result;
		}
		return null;
	}

	public static Message readNextMessage(InputStream stream, boolean client) throws IOException {

//		System.out.println(stream.toString());

		Message m = new Message();

		while (true) {
			try {
				Frame f = readNextFrame(stream, client);
				if (f == null) {
					throw new WseWebSocketException("Got null");
				}
				m.frames.add(f);

				if (f.isFin()) {
					return m;
				}
			} catch (Exception e) {
				throw new WseWebSocketException("Could not read next frame: " + e.getMessage(), e);
			}

		}
	}
	
	public static String getWebSocketAcceptFromKey(String key) {
		try {
			return StringUtils.printBase64Binary(
					MessageDigest.getInstance("SHA-1").digest((key + WebSocket.WEB_SOCKET_MAGIC_STR).getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Logger log = WSE.getLogger();
	private WebSocketEndpointImpl endpoint;
	private URI uri;
	private SSLAuth auth;

	public WebSocket(URI uri, WebSocketListener listener) {
		this(uri, null, listener);
	}

	public WebSocket(URI uri, SSLAuth auth, WebSocketListener listener) {
		this.uri = uri;
		this.auth = auth;
		this.endpoint = new WebSocketEndpointImpl(true);
		this.registerListener(listener);

		if (this.handler != null) {
			throw new WseWebSocketException("WebSocket has already been opened");
		}

		thread = new Thread(new Runnable() {
			public void run() {
				try {
					open();
				} catch (Exception e) {
					e.printStackTrace();
					log.severe("Failed to open websocket: " + e.getMessage());
				}
			}
		});
		thread.start();
	}

	private CallHandler handler;
	private HttpResult handshakeResult;

	private InputStream input;
	private OutputStream output;

	private Thread thread;
	public static final String WEB_SOCKET_MAGIC_STR = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

	private void open() throws IOException {
		try {
			doHandshake();
		} catch (Exception e) {
			throw new WseWebSocketException("Error during WebSocket handshake: " + e.getMessage(), e);
		}

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

	private void doHandshake() throws IOException {
		this.handler = new CallHandler(HttpMethod.GET, this.uri, null, this.auth);
		
		this.handler.setSoTimeout(1000 * 10);

		String controlKey = controlKey();
		this.handler.setWebSocketControlKey(controlKey);

		String expectedResponseKey = getWebSocketAcceptFromKey(controlKey);

		this.handshakeResult = handler.call();

		HeaderAttribute acc = this.handshakeResult.getHeader().getAttribute("Sec-WebSocket-Accept");
		if (acc == null || !acc.hasValue() || !expectedResponseKey.equals(acc.value)) {
			throw new WseWebSocketHandshakeException("Server responded with invalid accept key");
		}

		// notifications.js:28 WebSocket connection to 'ws://localhost:8505/chat'
		// failed: Error during WebSocket handshake: 'Connection' header value must
		// contain 'Upgrade'

		HeaderAttribute connection = this.handshakeResult.getHeader().getAttribute("Connection");
		if (connection == null || !connection.hasValue() || !"Upgrade".equals(connection.value))
			throw new WseWebSocketHandshakeException("'Connection' response header value must contain 'Upgrade'");

		HeaderAttribute upgrade = this.handshakeResult.getHeader().getAttribute("Upgrade");
		if (upgrade == null || !upgrade.hasValue() || !"websocket".equals(upgrade.value))
			throw new WseWebSocketHandshakeException("'Upgrade' response header value must contain 'websocket'");
	}

	private String controlKey() {
		byte[] key = new byte[8];
		new Random().nextBytes(key);
		String b64 = WSE.printBase64Binary(key);
		return b64;
	}

	public void sendMessage(byte opcode, StreamWriter writer) throws IOException {
		this.endpoint.sendMessage(opcode, writer);
	}

	public void pingAsync(PongListener listener) throws IOException {
		this.endpoint.pingAsync(listener);
	}

	public void close(String shutDownMessage) throws IOException {
		this.endpoint.close(shutDownMessage);
	}

	public void forceClose(String err, Throwable e) {
		log.severe("WebSocket forcing close: " + (e != null ? e.getMessage() : err));
		try {
			this.input.close(); // The input is connected to socket
		} catch (Exception e2) {
		}
		try {
			this.endpoint.forceClose(err);
		} catch (Exception e2) {
			log.severe(e2.getMessage());
		}
	}

	public ListenerRegistration registerListener(WebSocketListener listener) {
		return this.endpoint.registerListener(listener);
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

}
