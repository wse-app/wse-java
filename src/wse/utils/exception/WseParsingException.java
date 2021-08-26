package wse.utils.exception;

public class WseParsingException extends WseException {
	private static final long serialVersionUID = 7758918945111470916L;

	public WseParsingException(String message) {
		super(message);
	}

	public WseParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
