package wse.utils.exception;

public class WseWebSocketException extends WseException {
	private static final long serialVersionUID = 486529171433522386L;
	
	public WseWebSocketException() {
		super();
	}
	
	public WseWebSocketException(String message)
	{
		super(message);
	}
	
	public WseWebSocketException(Throwable cause)
	{
		super(cause);
	}

	public WseWebSocketException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseWebSocketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	
	
	

}
