package wse.utils.exception;

public class WseXMLParsingException extends WseParsingException{

	private static final long serialVersionUID = 5397990723469474391L;

	public WseXMLParsingException(String message) {
		super(message);
	}
	
	public WseXMLParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
