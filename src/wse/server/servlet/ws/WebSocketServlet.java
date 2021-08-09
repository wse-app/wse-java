package wse.server.servlet.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.servlet.HttpServlet;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.utils.ArrayUtils;
import wse.utils.HttpCodes;
import wse.utils.event.ListenerRegistration;
import wse.utils.stream.ProtectedInputStream;
import wse.utils.stream.ProtectedOutputStream;
import wse.utils.websocket.WebSocket;
import wse.utils.websocket.WebSocketCodes;
import wse.utils.websocket.WebSocketEndpoint;
import wse.utils.websocket.WebSocketEndpointImpl;
import wse.utils.websocket.WebSocketListener;
import wse.utils.writable.StreamWriter;

public abstract class WebSocketServlet extends HttpServlet implements WebSocketCodes, WebSocketEndpoint {

	private static Logger log = WSE.getLogger();

	public static final int[] SUPPORTED_VERSIONS = { 13 };

	public static final String UPGRADE_VALUE = "websocket", CONNECTION_VALUE = "Upgrade";

	public static final String ATTRIB_UPGRADE = "Upgrade", ATTRIB_CONNECTION = "Connection",
			ATTRIB_KEY = "Sec-WebSocket-Key", ATTRIB_VERSION = "Sec-WebSocket-Version",
			ATTRIB_ACCEPT = "Sec-WebSocket-Accept";

	private WebSocketEndpointImpl endpoint;

	@Override
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String upgrade_attrib = request.getAttributeValue(ATTRIB_UPGRADE);
		String connection_attrib = request.getAttributeValue(ATTRIB_CONNECTION);
		String key_attrib = request.getAttributeValue(ATTRIB_KEY);
		String version_attrib = request.getAttributeValue(ATTRIB_VERSION);

		if (upgrade_attrib == null) {
			response.sendError(HttpCodes.BAD_REQUEST, "Header attribute " + ATTRIB_UPGRADE + " missing");
			return;
		}
		if (connection_attrib == null) {

			response.sendError(HttpCodes.BAD_REQUEST, "Header attribute " + ATTRIB_CONNECTION + " missing");
			return;
		}
		if (key_attrib == null) {
			response.sendError(HttpCodes.BAD_REQUEST, "Header attribute " + ATTRIB_KEY + " missing");
			return;
		}
		if (version_attrib == null) {
			response.sendError(HttpCodes.BAD_REQUEST, "Header attribute " + ATTRIB_VERSION + " missing");
			return;
		}

		if (!UPGRADE_VALUE.equals(upgrade_attrib)) {
			response.sendError(HttpCodes.BAD_REQUEST, "Invalid header attribute value for " + ATTRIB_UPGRADE);
			return;
		}

		if (!CONNECTION_VALUE.equals(connection_attrib)) {
			response.sendError(HttpCodes.BAD_REQUEST, "Invalid header attribute value for " + ATTRIB_CONNECTION);
			return;
		}

		int version = Integer.parseInt(version_attrib);
		boolean supported = false;
		for (int v : WebSocketServlet.SUPPORTED_VERSIONS) {
			if (version == v) {
				supported = true;
				break;
			}
		}

		if (!supported) {
			response.setAttribute(ATTRIB_VERSION, ArrayUtils.join(WebSocketServlet.SUPPORTED_VERSIONS, ", "));
			response.sendError(HttpCodes.BAD_REQUEST, "WebSocket version " + version + " unsupported");
			return;
		}

		response.setAttribute(ATTRIB_UPGRADE, UPGRADE_VALUE);
		response.setAttribute(ATTRIB_CONNECTION, CONNECTION_VALUE);

		String response_key = WebSocket.getWebSocketAcceptFromKey(key_attrib);
		response.setAttribute(ATTRIB_ACCEPT, response_key);

		response.setStatusCode(101);
		response.writeHeader();
		response.flush();

		this.endpoint = new WebSocketEndpointImpl(false);
		this.endpoint.setInputStream(new ProtectedInputStream(request.getContent()));
		this.endpoint.setOutputStream(new ProtectedOutputStream(response));
		this.endpoint.registerListener(this);
		this.endpoint.onInit(request);
		try {
			this.endpoint.readLoop();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			this.forceClose(e.getMessage());
		}
	}

	public void sendMessage(final String message) throws IOException {
		sendMessage(OP_TEXT, new StreamWriter() {
			@Override
			public void writeToStream(OutputStream output, Charset charset) throws IOException {
				output.write(message.getBytes(charset));
			}
		});
	}

	@Override
	public void sendMessage(byte opcode, StreamWriter writer) throws IOException {
		this.endpoint.sendMessage(opcode, writer);
	}

	@Override
	public final void pingAsync(PongListener listener) throws IOException {
		this.endpoint.pingAsync(listener);
	}

	@Override
	public final void close(String shutDownMessage) throws IOException {
		this.endpoint.close(shutDownMessage);
	}

	@Override
	public final void forceClose(String err) {
		log.fine("WebSocket forcing close: " + err);
		this.endpoint.forceClose(err);
	}

	@Override
	public final ListenerRegistration registerListener(WebSocketListener listener) {
		return this.endpoint.registerListener(listener);
	}

}
