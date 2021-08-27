package wse.utils.exception;

public class SecurityRetry extends SHttpException {
	private static final long serialVersionUID = -3705111410121881250L;

	public SecurityRetry() {
	}

	public SecurityRetry(String message) {
		super(message);
	}

	public SecurityRetry(Throwable cause) {
		super(cause);
	}

	public SecurityRetry(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityRetry(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
