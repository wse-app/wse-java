package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public class ProtectedOutputStream extends WseOutputStream {

	public ProtectedOutputStream(OutputStream writeTo) {
		super(writeTo);
	}

	@Override
	public void close() throws IOException {
	}
}
