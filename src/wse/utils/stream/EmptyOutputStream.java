package wse.utils.stream;

import java.io.IOException;

public class EmptyOutputStream extends WseOutputStream {

	public EmptyOutputStream() {
		super(null);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
	}

	@Override
	public void write(int b) throws IOException {
	}

}
