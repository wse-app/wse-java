package wse.utils.exception;

public class WseSHttpDecryptionException extends WseException {
	private static final long serialVersionUID = 486529171433542386L;
	
	public WseSHttpDecryptionException() {
		super();
	}
	
	public WseSHttpDecryptionException(String message)
	{
		super(message);
	}
	
	public WseSHttpDecryptionException(Throwable cause)
	{
		super(cause);
	}

	public WseSHttpDecryptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseSHttpDecryptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	
	
	

}
