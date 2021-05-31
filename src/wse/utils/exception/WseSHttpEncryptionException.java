package wse.utils.exception;

public class WseSHttpEncryptionException extends WseException {
	private static final long serialVersionUID = 486529171433542386L;
	
	public WseSHttpEncryptionException() {
		super();
	}
	
	public WseSHttpEncryptionException(String message)
	{
		super(message);
	}
	
	public WseSHttpEncryptionException(Throwable cause)
	{
		super(cause);
	}

	public WseSHttpEncryptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseSHttpEncryptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	
	
	

}
