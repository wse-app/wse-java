package wse.utils.json;

public class JSONSyntaxException extends JSONException {

	private static final long serialVersionUID = 599247896597411993L;

	public JSONSyntaxException() {
		super();
	}

	public JSONSyntaxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JSONSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public JSONSyntaxException(String message) {
		super(message);
	}

	public JSONSyntaxException(Throwable cause) {
		super(cause);
	}

}
