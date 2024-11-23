package wse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WrappedConnection implements IOConnection {

	private final IOConnection wrapped;
	private final InputStream inOverride;
	private final OutputStream outOverride;

	public WrappedConnection(IOConnection wrapped, InputStream inOverride) {
		this(wrapped, inOverride, null);
	}

	public WrappedConnection(IOConnection wrapped, OutputStream outOverride) {
		this(wrapped, null, outOverride);
	}

	public WrappedConnection(InputStream inOverride, OutputStream outOverride) {
		this(null, inOverride, outOverride);
	}

	public WrappedConnection(IOConnection wrapped, InputStream inOverride, OutputStream outOverride) {
		this.wrapped = wrapped;
		this.inOverride = inOverride;
		this.outOverride = outOverride;
	}

	@Override
	public void close() throws IOException {
		if (wrapped != null) {
			wrapped.close();
		} else {
			if (inOverride != null)
				inOverride.close();
			if (outOverride != null)
				outOverride.close();
		}
	}

	@Override
	public boolean isOpen() throws IOException {
		return wrapped.isOpen();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return outOverride != null ? outOverride : wrapped.getOutputStream();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return inOverride != null ? inOverride : wrapped.getInputStream();
	}

	@Override
	public void connect() throws IOException {
		wrapped.connect();
	}
}
