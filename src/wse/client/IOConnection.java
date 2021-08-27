package wse.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IOConnection extends Closeable {
	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	void connect() throws IOException;

	boolean isOpen() throws IOException;
}
