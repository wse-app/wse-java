package wse.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import wse.WSE;
import wse.utils.Consumer;
import wse.utils.Consumers;
import wse.utils.HasOptions;
import wse.utils.IOptions;
import wse.utils.Option;
import wse.utils.Options;
import wse.utils.exception.WseConnectionException;
import wse.utils.ssl.SSLAuth;

public class SocketConnection implements IOConnection, HasOptions {

	/**
	 * Socket read() timeout in milliseconds. <br>
	 * <br>
	 * Defualt value is 10000 (10 seconds).
	 * 
	 * @see Socket#setSoTimeout(int)
	 * @see SocketOptions#SO_TIMEOUT
	 */
	public static final Option<Integer> SOCKET_TIMEOUT = new Option<Integer>(SocketConnection.class, "SOCKET_TIMEOUT",
			10000);

	/**
	 * Enable or disable {@link SocketOptions#TCP_NODELAY TCP_NODELAY} <br>
	 * <br>
	 * Default value is true (Nagle's algorithm disabled)
	 * 
	 * @see Socket#setTcpNoDelay(boolean)
	 * @see SocketOptions#TCP_NODELAY
	 */
	public static final Option<Boolean> SOCKET_TCP_NODELAY = new Option<Boolean>(SocketConnection.class,
			"SOCKET_TCP_NODELAY", true);

	/**
	 * Enable or disable REUSE_ADDR <br>
	 * <br>
	 * Default value is true (enabled)
	 * 
	 * @see Socket#setReuseAddress(boolean)
	 * @see SocketOptions#SO_REUSEADDR
	 */
	public static final Option<Boolean> SOCKET_REUSE_ADDRESS = new Option<Boolean>(SocketConnection.class,
			"SOCKET_REUSE_ADDRESS", true);

	/**
	 * Set an optional Socket processor that will receive the Socket after it has
	 * been created and before it is connected. <br>
	 * Default value is <code>null</code>
	 */
	public static final Option<Consumer<Socket>> SOCKET_PROCESSOR = new Option<>(SocketConnection.class,
			"SOCKET_PROCESSOR");

	/**
	 * Set an optional default SocketFactory to be used by the SocketConnection. If
	 * none is specified, <br>
	 * {@link SocketFactory#getDefault()} will be used for non-SSL and, <br>
	 * {@link SSLSocketFactory#getDefault()} will be used for SSL. <br>
	 * <br>
	 * Default value is <code>null</code> (meaning SocketFactory.getDefault()
	 * fallback)
	 * 
	 * @see SocketFactory#getDefault()
	 * @see SSLSocketFactory#getDefault()
	 */
	public static final Option<SocketFactory> SOCKET_FACTORY = new Option<>(SocketConnection.class, "SOCKET_FACTORY");

	private static final Logger LOG = WSE.getLogger();
	private final Options options = new Options();

	@Override
	public IOptions getOptions() {
		return options;
	}

	@Override
	public void setOptions(HasOptions other) {
		this.options.setOptions(other);
	}

	private final SSLAuth sslAuth;
	private final String host;
	private final int port;
	private final boolean ssl;

	private Socket socket;

	public SocketConnection(String host, int port) {
		this(null, false, host, port);
	}

	public SocketConnection(boolean ssl, String host, int port) {
		this(null, ssl, host, port);
	}

	public SocketConnection(SSLAuth sslAuth, boolean ssl, String host, int port) {
		this.sslAuth = sslAuth;
		this.ssl = ssl;
		this.host = host;
		this.port = port;
	}

	public void connect() throws IOException {
		socket = this.ssl ? connectSSL() : connectNonSSL();

		if (!socket.isConnected() || socket.isClosed()) {
			LOG.severe("Socket failed to open");
			throw new WseConnectionException("Socket failed to open");
		}
	}

	private Socket connectSSL() throws IOException {
		SSLSocketFactory factory = getSSLSocketFactory();
		SSLSocket socket = (SSLSocket) factory.createSocket();

		options(socket);

		SocketAddress address = getSocketAddress();
		socket.connect(address);

		socket.setUseClientMode(true);
		socket.startHandshake();

		return socket;
	}

	private Socket connectNonSSL() throws IOException {
		SocketFactory factory = getNonSSLSocketFactory();
		Socket socket = factory.createSocket();

		options(socket);

		SocketAddress address = getSocketAddress();
		socket.connect(address);

		return socket;
	}

	private void options(Socket socket) throws SocketException {
		socket.setReuseAddress(options.get(SocketConnection.SOCKET_REUSE_ADDRESS));
		socket.setSoTimeout(options.get(SocketConnection.SOCKET_TIMEOUT));
		socket.setTcpNoDelay(options.get(SocketConnection.SOCKET_TCP_NODELAY));

		options.get(SocketConnection.SOCKET_PROCESSOR, Consumers.<Socket>empty()).consume(socket);
	}

	private SSLSocketFactory getSSLSocketFactory() {
		SocketFactory defFactory = options.get(SocketConnection.SOCKET_FACTORY);

		if (defFactory != null && defFactory instanceof SSLSocketFactory) {
			return (SSLSocketFactory) defFactory;
		}

		if (sslAuth != null) {
			sslAuth.getTrustManagerImpl().setExpectedHost(this.host);
			return sslAuth.getSSLSocketFactory();
		}
		return (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	private SocketFactory getNonSSLSocketFactory() {
		SocketFactory defFactory = options.get(SocketConnection.SOCKET_FACTORY);

		if (defFactory != null && !(defFactory instanceof SSLSocketFactory)) {
			return defFactory;
		}

		return SocketFactory.getDefault();
	}

	@Override
	public InputStream getInputStream() {
		if (socket == null)
			throw new IllegalStateException("This SocketConnection is not connected");
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			throw new WseConnectionException("Failed to retrieve InputStream: " + e.getMessage(), e);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		if (socket == null)
			throw new IllegalStateException("This SocketConnection is not connected");
		try {
			return socket.getOutputStream();
		} catch (IOException e) {
			throw new WseConnectionException("Failed to retrieve OutputStream: " + e.getMessage(), e);
		}
	}

	private SocketAddress getSocketAddress() {
		return new InetSocketAddress(getHost(), getPort());
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public SSLAuth getSSLAuth() {
		return sslAuth;
	}

	public void close() throws IOException {
		socket.close();
	}
}
