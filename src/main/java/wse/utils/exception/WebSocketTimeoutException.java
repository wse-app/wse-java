package wse.utils.exception;

public class WebSocketTimeoutException extends WebSocketException {
	private static final long serialVersionUID = 486529171433522386L;

	public WebSocketTimeoutException() {
		super();
	}

	public WebSocketTimeoutException(String message) {
		super(message);
	}

	public WebSocketTimeoutException(Throwable cause) {
		super(cause);
	}

	public WebSocketTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebSocketTimeoutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
