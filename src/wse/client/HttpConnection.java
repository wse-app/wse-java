package wse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import wse.utils.Consumer;
import wse.utils.HttpResult;
import wse.utils.exception.WseConnectionException;
import wse.utils.http.HttpBuilder;
import wse.utils.ssl.SSLAuth;
import wse.utils.stream.CountingInputStream;

public class HttpConnection {
	private boolean ssl;

	private String target_address;
	private int target_port;

//	public String faultMessage;

	private Socket socket;

	private InputStream inputStream;
	private OutputStream outputStream;

	private HttpResult result;
	private SSLAuth sslAuth;

	private int soTimeout = 10000;

	private SocketFactory defSocketFactory;
	private Consumer<Socket> socketProcessor;

	public HttpConnection(SSLAuth sslAuth, String address, int port) {
		this.sslAuth = sslAuth;
		this.target_address = address;
		this.target_port = port;
	}

	public void setSocketFactory(SocketFactory factory) {
		this.defSocketFactory = factory;
	}

	public void setSocketProcessor(Consumer<Socket> processor) {
		this.socketProcessor = processor;
	}

	public void setSoTimeout(int timeout) throws SocketException {
		soTimeout = timeout;
		if (socket != null) {
			socket.setSoTimeout(timeout);
		}
	}

	public void setUseSSL(boolean ssl) {
		this.ssl = ssl;
	}

	public void connect(Logger logger) {
		try {

			InetSocketAddress address = new InetSocketAddress(target_address, target_port);

			if (ssl) {

				SSLSocketFactory sslFactory;
				SSLSocket socket;

				if (defSocketFactory != null && defSocketFactory instanceof SSLSocketFactory) {
					sslFactory = (SSLSocketFactory) defSocketFactory;
				} else {
					if (sslAuth != null) {
						sslAuth.getTrustManagerImpl().setExpectedHost(target_address);
						sslFactory = sslAuth.getSSLSocketFactory();
					} else {
						sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
					}
				}

				socket = (SSLSocket) sslFactory.createSocket();
				socket.setReuseAddress(true);

				if (socketProcessor != null) {
					socketProcessor.consume(socket);
				}

				socket.connect(address);

				socket.setUseClientMode(true);
				socket.startHandshake();
				this.socket = socket;

			} else {

				SocketFactory factory;
				Socket socket;

				if (defSocketFactory != null && !(defSocketFactory instanceof SSLSocketFactory)) {
					factory = defSocketFactory;
				} else {
					factory = SocketFactory.getDefault();
				}

				socket = factory.createSocket();

				if (socketProcessor != null) {
					socketProcessor.consume(socket);
				}

				socket.connect(address);
				this.socket = socket;
//				socket = new Socket(target_address, target_port);

			}

			socket.setSoTimeout(soTimeout);
			socket.setTcpNoDelay(true);

			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			if (!socket.isConnected() || socket.isClosed()) {
				if (logger != null)
					logger.severe("Socket failed to open");
				throw new WseConnectionException("Socket failed to open");
			}

		} catch (Exception e) {
			if (logger != null)
				logger.severe("Failed to connect: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			throw new WseConnectionException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
//		catch (IOException e) {
//			if (logger != null)
//				logger.error("Failed to connect: " + e.getClass().getSimpleName() + ": " + e.getMessage());
//			throw new WseConnectionException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
//		} catch (SecurityException e) {
//			if (logger != null)
//				logger.error("SecurityException: " + e.getMessage());
//			throw new WseConnectionException("SecurityException: " + e.getMessage(), e);
//		}
	}

	public void read() {
		read(false);
	}

	public void read(boolean modifyContent) {
		if (inputStream == null) {
			throw new WseConnectionException("Failed to open input stream");
		}

		try {
			result = HttpBuilder.read(new CountingInputStream(inputStream), modifyContent);

		} catch (Exception e) {
			throw new WseConnectionException("Failed to read: " + e.getMessage(), e);
		}
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public HttpResult getRecievedHttp() {
		return this.result;
	}

	public boolean close() {

		try {
			socket.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean closeOutput() {
		try {
			socket.shutdownOutput();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
