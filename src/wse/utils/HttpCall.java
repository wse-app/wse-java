package wse.utils;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import wse.utils.ssl.SSLAuth;
import wse.utils.ssl.SSLManager;

/**
 * 
 * This class is the parent class for sending and recieving data through Web
 * Service Engine. Call classes should inherit from this class to be able to
 * send and recieve data through the inheritable methods load and create.
 * 
 * 
 * @author Greattech
 *
 */
public abstract class HttpCall extends Target {
	public HttpCall() {
	}

	public HttpCall(Target target) {
		super(target);
	}

	public HttpCall(String uri) throws URISyntaxException {
		super(uri);
	}

	public HttpCall(URI uri) {
		super(uri);
	}

	private SSLSocketFactory sslSocketFactory;
	private SocketFactory socketFactory;
	private Consumer<Socket> socketProcessor;
	private Integer soTimeout;

	//
	// SSL
	//

	protected SSLAuth private_store;

	/**
	 * Binds this instance to the specified SSLStore, use
	 * {@link SSLManager#bind(Class, SSLAuth)} to bind permanently. <br>
	 * <br>
	 * Bindings in order of priority:<br>
	 * - Per-instance binding. <br>
	 * - Service binding if this HttpCall was created by one. (See
	 * {@link Service#bindToSSLStore(SSLAuth)}}<br>
	 * - Permanent binding. (See {@link SSLManager#bind(Class, SSLAuth)}) <br>
	 * - Default binding. (See {@link SSLManager#bindDefault(SSLAuth)}) <br>
	 * 
	 * @param sslStore The SSLStore to bind to.
	 */
	public void bindToSSLStore(SSLAuth sslStore) {
		this.private_store = sslStore;
	}

	/**
	 * Retrieves the socket factory that this HttpCall is requested to use. This can
	 * be overridden by {@link #setSocketFactory(SocketFactory)}<br>
	 * 
	 * If the used protocol is secure, ssl socket factories are prioritized, as
	 * specified by {@link Protocol#getDefaultSocketFactory()}.
	 * 
	 * @return the socket factory that this HttpCall should use.
	 */
	public SocketFactory getSocketFactory() {
		return getSocketFactory(null);
	}

	/**
	 * Retrieves the socket factory that this HttpCall is requested to use. This can
	 * be overridden by {@link #setSocketFactory(SocketFactory)}<br>
	 * 
	 * If the used protocol is secure, ssl socket factories are prioritized, as
	 * specified by {@link Protocol#getDefaultSocketFactory()}.
	 * 
	 * @param protocolOverride A Protocol to be used instead of
	 *                         {@link #getTargetSchemeAsProtocol()}
	 * @return the socket factory that this HttpCall should use.
	 */
	public SocketFactory getSocketFactory(Protocol protocolOverride) {
		Protocol protocol = protocolOverride != null ? protocolOverride : getTargetSchemeAsProtocol();

		if (protocol == null) {
			return SocketFactory.getDefault();
		}

		if (protocol.isSecure()) {
			if (sslSocketFactory != null)
				return sslSocketFactory;

			SSLAuth sslAuth = getSSLStore();
			if (sslAuth != null)
				return sslAuth.getSSLSocketFactory();

		} else {

			if (socketFactory != null) {
				return socketFactory;
			}
		}

		return protocol.getDefaultSocketFactory();
	}

	/**
	 * Sets the SocketFactory or SSLSocketFactory to be used by this HttpCall. If
	 * not specified, default SocketFactories will be used.
	 * 
	 * @param factory
	 */
	public void setSocketFactory(SocketFactory factory) {
		if (factory instanceof SSLSocketFactory) {
			this.sslSocketFactory = (SSLSocketFactory) factory;
		} else {
			this.socketFactory = factory;
		}
	}

	/**
	 * Specify a Socket processor that will receive Sockets before they are
	 * connected
	 * 
	 * @param socketProcessor A Consumer that will receive Sockets before they are
	 *                        connected
	 */
	public void setSocketProcessor(Consumer<Socket> socketProcessor) {
		this.socketProcessor = socketProcessor;
	}

	/**
	 * @return The Socket processor specified by
	 *         {@link #setSocketProcessor(Consumer)}
	 */
	public Consumer<Socket> getSocketProcessor() {
		return this.socketProcessor;
	}

	/**
	 * As specified by {@link Socket#setSoTimeout(int)}
	 * @param timeout
	 * @see Socket#setSoTimeout(int)
	 */
	public void setSoTimeout(Integer timeout) {
		this.soTimeout = timeout;
	}
	
	
	/**
	 * @return the SO_TIMEOUT specified by {@link #setSoTimeout(Integer)}
	 */
	public Integer getSoTimeout() {
		return this.soTimeout;
	}
	
	/**
	 * 
	 * Returns the most relevant SSLAuth instance for this caller. <br>
	 * Bindings in order of priority:<br>
	 * - Per-instance binding.<br>
	 * - Service binding if this HttpCall was created by one. (See
	 * {@link Service#bindToSSLStore(SSLAuth)}}<br>
	 * - Permanent binding. (See {@link SSLManager#bind(Class, SSLAuth)}) <br>
	 * - Default binding. (See {@link SSLManager#bindDefault(SSLAuth)}) <br>
	 * 
	 * @return
	 */
	public SSLAuth getSSLStore() {
		if (this.private_store != null)
			return this.private_store;

		return SSLManager.getBinding(this.getClass());
	}

	/**
	 * 
	 * The String returned is a reference String for the SOAP-route.
	 * 
	 * @return
	 */
	protected abstract String getServiceName();

	protected abstract String getSoapAction();

	public String toString() {
		return getServiceName();
	}

	public static abstract class AsyncCallback<T> {
		public abstract void onSuccess(T response);

		public void onFail(Throwable cause) {

		}
	}

}
