package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class ProtectedInputStream extends WseInputStream {

	public ProtectedInputStream(InputStream writeTo) {
		super(writeTo);
	}

	@Override
	public void close() throws IOException {
	}

}
