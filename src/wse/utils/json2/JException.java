package wse.utils.json2;

public class JException extends RuntimeException {
	private static final long serialVersionUID = -3832295584615239809L;

	public JException(int row, int column, String message) {
		this(row, column, message, null);
	}
	
	public JException(int row, int column, String message, Throwable t) {
		this(String.format("%d;%d; %s", row, column, message), t);
	}


	public JException(String message, Throwable cause) {
		super(message, cause);
	}

}
