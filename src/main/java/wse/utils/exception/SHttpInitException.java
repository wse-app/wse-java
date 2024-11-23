package wse.utils.exception;

public class SHttpInitException extends SHttpException {
	private static final long serialVersionUID = 486529171433542386L;

	public SHttpInitException() {
		super();
	}

	public SHttpInitException(String message) {
		super(message);
	}

	public SHttpInitException(Throwable cause) {
		super(cause);
	}

	public SHttpInitException(String message, Throwable cause) {
		super(message, cause);
	}

	public SHttpInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
