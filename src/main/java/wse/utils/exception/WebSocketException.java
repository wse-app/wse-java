package wse.utils.exception;

public class WebSocketException extends WseException {
	private static final long serialVersionUID = 486529171433522386L;

	public WebSocketException() {
		super();
	}

	public WebSocketException(String message) {
		super(message);
	}

	public WebSocketException(Throwable cause) {
		super(cause);
	}

	public WebSocketException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebSocketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
