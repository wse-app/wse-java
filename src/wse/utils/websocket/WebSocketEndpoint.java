package wse.utils.websocket;

import java.io.IOException;

import wse.server.servlet.ws.PongListener;
import wse.utils.event.ListenerRegistration;
import wse.utils.writable.StreamWriter;

public interface WebSocketEndpoint extends WebSocketListener {
//	public void readLoop() throws IOException;
	public void sendMessage(byte opcode, StreamWriter writer) throws IOException;

	public void pingAsync(PongListener listener) throws IOException;

	public void close(String shutDownMessage) throws IOException;

	public void forceClose(String err) throws IOException;

	public ListenerRegistration registerListener(WebSocketListener listener);
}
