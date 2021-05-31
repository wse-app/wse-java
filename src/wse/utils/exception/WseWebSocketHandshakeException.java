package wse.utils.exception;

public class WseWebSocketHandshakeException extends WseWebSocketException {
	private static final long serialVersionUID = 486529171433522386L;
	
	public WseWebSocketHandshakeException() {
		super();
	}
	
	public WseWebSocketHandshakeException(String message)
	{
		super(message);
	}
	
	public WseWebSocketHandshakeException(Throwable cause)
	{
		super(cause);
	}

	public WseWebSocketHandshakeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseWebSocketHandshakeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	
	
	

}
