package wse.utils.exception;

public class WseHttpStatusCodeException extends HttpException {

	private static final long serialVersionUID = -2126925313175637910L;

	public WseHttpStatusCodeException(String message, int statusCode) {
		super(message, statusCode);
	}

}
