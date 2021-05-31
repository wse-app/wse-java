package wse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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

	public HttpConnection(SSLAuth sslAuth, String address, int port) {
		this.sslAuth = sslAuth;
		this.target_address = address;
		this.target_port = port;
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
			if (ssl) {
				if (sslAuth != null) {

					sslAuth.getTrustManagerImpl().setExpectedHost(target_address);

					SSLSocket socket = (SSLSocket) sslAuth.getSSLSocketFactory().createSocket();
					socket.setReuseAddress(true);
					socket.connect(new InetSocketAddress(target_address, target_port));
					socket.setUseClientMode(true);
					socket.startHandshake();
					this.socket = socket;
				} else {
					SSLSocketFactory sslsf = (SSLSocketFactory) SSLSocketFactory.getDefault();
					SSLSocket socket = (SSLSocket) sslsf.createSocket(target_address, target_port);

					socket.startHandshake();
					this.socket = socket;
				}
			} else {
				socket = new Socket(target_address, target_port);
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

		} catch(Exception e) {
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
