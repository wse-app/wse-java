package wse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IOConnection {
	InputStream getInputStream();
	OutputStream getOutputStream();
	void connect() throws IOException;
	void close() throws IOException;
}
