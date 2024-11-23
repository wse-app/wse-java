package wse.utils.websocket;

import java.io.IOException;
import java.io.InputStream;

import wse.utils.http.HttpHeader;

public interface WebSocketListener {
	public void onInit(HttpHeader header) throws IOException;

	public void onMessage(InputStream message) throws IOException;

	public void onClose(boolean controlledShutdown, String shutdownMessage) throws IOException;
	
	public void onException(Throwable t);
}
