package wse.utils.writer;

import java.nio.charset.Charset;

import wse.utils.MimeType;

public class StringWriter extends ByteArrayWriter {

	public StringWriter(String value) {
		this(value, MimeType.text.plain);
	}

	public StringWriter(String value, MimeType mt) {
		this(value, mt, Charset.forName("UTF-8"));
	}

	public StringWriter(String value, MimeType mt, Charset cs) {
		super(value == null ? new byte[0] : value.getBytes(cs), mt, cs);
	}

}
