package wse.utils;

public class OperationResult<O extends ComplexType> {

	private O result;
	private Throwable cause;

	public OperationResult(O result) {
		this.result = result;
	}

	public OperationResult(Throwable cause) {
		this.cause = cause;
	}

	public void setResult(O result) {
		this.result = result;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public O getResult() {
		return result;
	}

	public Throwable getCause() {
		return cause;
	}

}
