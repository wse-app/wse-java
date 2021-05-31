package wse.utils.exception;

public class WseInitException extends WseException {
	private static final long serialVersionUID = 486529171433542386L;
	
	public WseInitException() {
		super();
	}
	
	public WseInitException(String message)
	{
		super(message);
	}
	
	public WseInitException(Throwable cause)
	{
		super(cause);
	}

	public WseInitException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	
	
	

}
