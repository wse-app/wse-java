package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public interface IsInputStream {
	int read() throws IOException;

	int read(byte[] b) throws IOException;

	int read(byte[] b, int off, int len) throws IOException;

	long skip(long n) throws IOException;

	int available() throws IOException;

	void close() throws IOException;

	InputStream asInputStream();
}
