package wse.server.servlet.ws;

public interface PongListener {
	public void onPong(boolean result, long took);
}
