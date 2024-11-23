package wse.utils.exception;

public class WebSocketHandshakeException extends WebSocketException {
	private static final long serialVersionUID = 486529171433522386L;

	public WebSocketHandshakeException() {
		super();
	}

	public WebSocketHandshakeException(String message) {
		super(message);
	}

	public WebSocketHandshakeException(Throwable cause) {
		super(cause);
	}

	public WebSocketHandshakeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebSocketHandshakeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
