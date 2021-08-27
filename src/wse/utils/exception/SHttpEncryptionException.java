package wse.utils.exception;

public class SHttpEncryptionException extends SHttpException {
	private static final long serialVersionUID = 486529171433542386L;

	public SHttpEncryptionException() {
		super();
	}

	public SHttpEncryptionException(String message) {
		super(message);
	}

	public SHttpEncryptionException(Throwable cause) {
		super(cause);
	}

	public SHttpEncryptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SHttpEncryptionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
