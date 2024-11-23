package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class ProtectedInputStream extends WseInputStream {

	public ProtectedInputStream(InputStream readFrom) {
		super(readFrom);
	}

	@Override
	public void close() throws IOException {
	}

}
