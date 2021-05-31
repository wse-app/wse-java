package wse.utils.websocket;

import java.io.InputStream;

import wse.utils.http.HttpHeader;

public interface WebSocketListener {
	public void onInit(HttpHeader header);
	public void onMessage(InputStream message);
	public void onClose(boolean controlledShutdown, String shutdownMessage);
}
