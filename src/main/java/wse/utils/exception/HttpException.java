package wse.utils.exception;

public class HttpException extends WseException {

	private static final long serialVersionUID = 739404036417835870L;

	Integer sendCode;

	public HttpException(String message) {
		super(message);
	}

	public HttpException(String message, int responseCode) {
		super(message);
		this.sendCode = responseCode;
	}

	public HttpException(String message, Throwable e) {
		super(message, e);
	}

	public HttpException(String message, int responseCode, Throwable e) {
		super(message, e);
		this.sendCode = responseCode;
	}

	public Integer getStatusCode() {
		return sendCode;
	}

	public void setStatusCode(Integer sendCode) {
		this.sendCode = sendCode;
	}

}
