package wse.utils.exception;

public class WseSHttpException extends WseException {
	private static final long serialVersionUID = 486529171433542386L;
	
	public WseSHttpException() {
		super();
	}
	
	public WseSHttpException(String message)
	{
		super(message);
	}
	
	public WseSHttpException(Throwable cause)
	{
		super(cause);
	}

	public WseSHttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseSHttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	
	
	

}
