package wse.utils.http;

public interface ErrorFormatter {

	public static final ErrorFormatter DEFAULT = new ErrorFormatter() {

		@Override
		public byte[] error(int code, Throwable cause, HttpAttributeList attributes) {
			if (cause == null)
				return null;
			return error(code, cause.getClass().getSimpleName() + ": " + cause.getMessage(), attributes);
		}

		@Override
		public byte[] error(int code, String message, HttpAttributeList attributes) {
			if (message == null)
				return null;
			return message.getBytes();
		}
	};

	public byte[] error(int code, String message, HttpAttributeList attributes);

	public byte[] error(int code, Throwable cause, HttpAttributeList attributes);

}
