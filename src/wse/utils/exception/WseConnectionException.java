package wse.utils.exception;

public class WseConnectionException extends WseException {

	private static final long serialVersionUID = -8752608548443144656L;

	public WseConnectionException(Throwable cause) {
		super(cause);
	}

	public WseConnectionException(String message) {
		super(message);
	}

	public WseConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
