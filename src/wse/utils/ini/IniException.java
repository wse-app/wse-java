package wse.utils.ini;

import wse.utils.exception.WseParsingException;
import wse.utils.internal.HasRowColumn;

public class IniException extends WseParsingException {
	private static final long serialVersionUID = -3832295584615239809L;

	public IniException(HasRowColumn pos, String message) {
		this(pos.getRow(), pos.getColumn(), message);
	}

	public IniException(int row, int column, String message) {
		this(row, column, message, null);
	}

	public IniException(int row, int column, String message, Throwable t) {
		this(String.format("%d;%d; %s", row, column, message), t);
	}

	public IniException(String message, Throwable cause) {
		super(message, cause);
	}
}
