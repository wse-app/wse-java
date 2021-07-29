package wse.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

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
public abstract class HttpCall extends Target implements IOptions {
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

	//
	// Options
	//

	private final Options options = new Options();

	@Override
	public IOptions getOptions() {
		return options;
	}

	@Override
	public <T> T get(Option<T> option) {
		return options.get(option);
	}

	@Override
	public <T> T get(Option<T> option, T def) {
		return options.get(option, def);
	}

	@Override
	public <T> void set(Option<T> option, T value) {
		options.set(option, value);
	}

	@Override
	public void setOptions(IOptions other) {
		options.setOptions(other);
	}

	@Override
	public void setOptions(HasOptions other) {
		this.options.setOptions(other);
	}

	@Override
	public Map<Option<?>, Object> getAll() {
		return options.getAll();
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
