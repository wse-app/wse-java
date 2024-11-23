package wse.utils.exception;

public class SHttpDecryptionException extends SHttpException {
	private static final long serialVersionUID = 486529171433542386L;

	public SHttpDecryptionException() {
		super();
	}

	public SHttpDecryptionException(String message) {
		super(message);
	}

	public SHttpDecryptionException(Throwable cause) {
		super(cause);
	}

	public SHttpDecryptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SHttpDecryptionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
