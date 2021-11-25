package wse.utils.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import wse.server.servlet.ws.PongListener;
import wse.utils.SerializationWriter;
import wse.utils.event.ListenerRegistration;
import wse.utils.exception.WseException;
import wse.utils.exception.WebSocketException;
import wse.utils.http.HttpHeader;
import wse.utils.stream.WS13OutputStream;
import wse.utils.websocket.WebSocket.Message;
import wse.utils.writable.StreamWriter;

public class WebSocketEndpointImpl implements WebSocketEndpoint, WebSocketCodes {

	private boolean isCloseRequestedByMe = false;
	private boolean isCloseRequestedByOther = false;
	private boolean isClosed = false;

	private InputStream input;

	private final Object OUTPUT_LOCK = new Object();
	private WS13OutputStream output;

	private long ping_time;
	private List<PongListener> pingListeners = new ArrayList<>();

	private final boolean isClient;
	private boolean useRandomKey = false;

	private boolean wasTimedOut = false;

	public WebSocketEndpointImpl(boolean isClient) {
		this.isClient = isClient;
	}

	public void setInputStream(InputStream input) {
		this.input = input;
	}

	public void setOutputStream(OutputStream output) {
		this.output = new WS13OutputStream(output, isClient);
		this.output.setUseRandomMaskKey(this.useRandomKey);
	}

	public void setUseRandomMaskKey(boolean useRandomKey) {
		this.useRandomKey = useRandomKey;
		if (this.output != null)
			this.output.setUseRandomMaskKey(this.useRandomKey);
	}

	public final void readLoop() throws IOException {
		try {
			while (!this.isClosed) {
				try {
					Message message = WebSocket.readNextMessage(this.input, this.isClient);
					if (message == null)
						continue;
					gotMessage(message);
				} catch (Exception e) {
					if (WseException.isCausedBy(e, SocketTimeoutException.class)) {

						if (wasTimedOut) {
							// Got no response from last ping
							this.forceClose("Connection timed out");
							break;
						} else {
							wasTimedOut = true;
							pingAsync(null);
						}
					} else {
						throw e;
					}
				}
			}
		} catch (Exception e) {
			if (!(this.isCloseRequestedByMe && this.isCloseRequestedByOther
					&& WseException.isCausedBy(e, SocketException.class))) {
				throw new WebSocketException("Exception on read: " + e.getMessage(), e);
			}
		}
	}

	public void sendMessage(byte opcode, StreamWriter writer) throws IOException {
		if (isClosed)
			throw new WebSocketException("WebSocket has been closed");
		if (isCloseRequestedByMe)
			throw new WebSocketException("WebSocket has been requested to close");
		synchronized (OUTPUT_LOCK) {
			output.setOpCode(opcode);
			if (writer != null) {
				writer.writeToStream(output, StandardCharsets.UTF_8);
				if (!output.isReadyForNextMessage()) {
					output.flush();
				}
			} else {
				output.flush();
			}

		}
	}

	public void pingAsync(PongListener listener) throws IOException {
		synchronized (pingListeners) {
			if (listener != null)
				pingListeners.add(listener);

			if (pingListeners.size() > 1) {
				return;
			}
		}
		ping_time = System.currentTimeMillis();
		sendMessage(OP_PING, new StreamWriter() {

			@Override
			public void writeToStream(OutputStream output, Charset charset) throws IOException {
				output.write("ping".getBytes(charset));
				output.flush();
			}
		});
	}

	private void pong(final byte[] data) throws IOException {
		sendMessage(OP_PONG, new StreamWriter() {
			@Override
			public void writeToStream(OutputStream output, Charset charset) throws IOException {
				output.write(data);
				output.flush();
			}
		});
	}

	private final void gotMessage(Message message) {
		this.wasTimedOut = false;

		if (!message.isEveryFrameMasked() && !this.isClient) {
			forceClose("Got request with unmasked frames");
			return;
		}

		if (message.getOPCode() == OP_CLOSE && !this.isCloseRequestedByOther) {
			if (this.isCloseRequestedByMe) {
				this.isCloseRequestedByOther = true;
				this.isClosed = true;

				this.onClose(true, new String(message.getFrames().get(0).getPayload()));
			} else {
				try {
					String msg = new String(message.getFrames().get(0).getPayload());
					this.isCloseRequestedByOther = true;
					close(msg);
					this.isClosed = true;
					onClose(true, new String(msg));
					this.input.close();
				} catch (IOException e) {
					e.printStackTrace();
					forceClose(e.getMessage());
				}
			}
			return;
		}

		if (this.isCloseRequestedByMe || this.isCloseRequestedByOther)
			throw new WebSocketException(
					"Got " + WebSocket.getCodeName(message.getOPCode()) + " after close was requested");

		if (message.getOPCode() == OP_PONG) {
			long took = System.currentTimeMillis() - ping_time;
			synchronized (pingListeners) {
				for (PongListener pl : pingListeners)
					pl.onPong(true, took);
				pingListeners.clear();
			}
			return;
		}

		if (message.getOPCode() == OP_PING) {
			try {
				pong(message.getFrames().get(0).getPayload());
			} catch (IOException e) {
				e.printStackTrace();
				forceClose(e.getMessage());
			}
			return;
		}
		try {
			onMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<WebSocketListener> listeners = new ArrayList<>();

	@Override
	public ListenerRegistration registerListener(final WebSocketListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}

		return new ListenerRegistration() {
			@Override
			public void unregister() {
				synchronized (listeners) {
					listeners.remove(listener);
				}
			}
		};
	}

	public void onInit(HttpHeader request) throws IOException {
		synchronized (listeners) {
			for (WebSocketListener l : listeners)
				l.onInit(request);
		}
	}

	public void onMessage(Message message) throws IOException {
		synchronized (listeners) {
			for (WebSocketListener l : listeners)
				l.onMessage(message.inputStream());
		}
	}

	public void onClose(boolean controlledShutdown, String shutdownMessage) {
		synchronized (listeners) {
			for (WebSocketListener l : listeners) {
				try {
					l.onClose(controlledShutdown, shutdownMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void close(String shutDownMessage) throws IOException {
		final byte[] data = shutDownMessage.getBytes();
		sendMessage(OP_CLOSE, new StreamWriter() {
			@Override
			public void writeToStream(OutputStream output, Charset charset) throws IOException {
				output.write(SerializationWriter.getBytes((short) 1000));
				output.write(data);
				output.flush();
			}
		});
		isCloseRequestedByMe = true;
	}

	public void forceClose(String err) {
		isClosed = true;
		onClose(false, err);
	}

	@Override
	public void onMessage(InputStream message) {
	}
}
