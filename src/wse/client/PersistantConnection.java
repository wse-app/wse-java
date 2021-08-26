package wse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class PersistantConnection implements IOConnection {
	final long creTime;

	Integer timeout;
	Integer max;
	IOConnection connection;

	int count;

	public PersistantConnection(IOConnection connection) {
		this(connection, null, null);
	}
	
	public PersistantConnection(IOConnection connection, Integer timeout, Integer max) {
		super();
		this.timeout = timeout;
		this.max = max;
		this.connection = connection;

		this.creTime = System.currentTimeMillis();
	}

	public Integer getTimeout() {
		return timeout;
	}

	public Integer getMax() {
		return max;
	}

	public IOConnection getConnections() {
		return connection;
	}

	@Override
	public void close() throws IOException {
		connection.close();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return connection.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return connection.getOutputStream();
	}

	@Override
	public void connect() throws IOException {
		connection.connect();
	}

	@Override
	public boolean isOpen() throws IOException {
		return connection.isOpen();
	}

	public boolean isValid() throws IOException {
		if (!isOpen())
			return false;
		
		if (timeout != null) {
			long age = System.currentTimeMillis() - creTime;
			age /= 1000;
			if (age >= timeout)
				return false;
		}

		if (max != null) {
			if (count > max)
				return false;
		}

		return true;
	}

}