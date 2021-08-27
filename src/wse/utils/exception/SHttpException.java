package wse.utils.exception;

public class SHttpException extends WseException {
	private static final long serialVersionUID = 486529171433542386L;

	public SHttpException() {
		super();
	}

	public SHttpException(String message) {
		super(message);
	}

	public SHttpException(Throwable cause) {
		super(cause);
	}

	public SHttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public SHttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
