package wse.utils.types;

import wse.utils.exception.WseException;

public class SimpleTypeException extends WseException{
	private static final long serialVersionUID = 989981743133992072L;

	public SimpleTypeException() {
		super();
	}

	public SimpleTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SimpleTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SimpleTypeException(String message) {
		super(message);
	}

	public SimpleTypeException(Throwable cause) {
		super(cause);
	}

	
	
}
