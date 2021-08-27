package wse.utils.exception;

import wse.utils.internal.HasRowColumn;

public class JSONException extends WseException {
	private static final long serialVersionUID = -3832295584615239809L;

	public JSONException(HasRowColumn pos, String message) {
		this(pos.getRow(), pos.getColumn(), message);
	}
	
	public JSONException(int row, int column, String message) {
		this(row, column, message, null);
	}
	
	public JSONException(int row, int column, String message, Throwable t) {
		this(String.format("%d;%d; %s", row, column, message), t);
	}


	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}

}
