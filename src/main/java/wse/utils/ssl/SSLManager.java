package wse.utils.ssl;

import java.util.HashMap;
import java.util.Map;

import wse.utils.Service;

public class SSLManager {

	private static Map<Class<?>, SSLAuth> bindings = new HashMap<>();
	private static SSLAuth def_auth;

	/**
	 * 
	 * Bind the specified HttpCall class to the SSLStore. per-instance bindings have
	 * higher priority. This does not have any effect on a server.
	 * 
	 * @param call  The HttpCall class to permanently bind.
	 * @param store The SSLStore to bind to.
	 */
	public static void bind(Class<?> call, SSLAuth store) {
		bindings.put(call, store);
	}

	/**
	 * 
	 * Bind the specified Service to the SSLStore. This is not a permanent binding,
	 * and is equivalent to calling service.bindToSSLStore(store); This does not
	 * have any effect on a server.
	 * 
	 * @param service The service to bind.
	 * @param store   The SSLStore to bind to.
	 */
	public static void bind(Service<?> service, SSLAuth store) {
		service.bindToSSLStore(store);
	}

	/**
	 * 
	 * Set the default SSLAuth for all services using WSE. This does not have any
	 * effect on a server.
	 * 
	 * @param service The service to bind.
	 * @param store   The SSLStore to bind to.
	 */
	public static void bindDefault(SSLAuth store) {
		def_auth = store;
	}

	/**
	 * Retrieves the SSLStore that the specified HttpCall is permanently bound to.
	 * This does not have any effect on a server.
	 * 
	 * @param caller The HttpCall that has a binding.
	 * @return The SSLStore that the specified HttpCall is permanently bound to, or
	 *         null if it has no binding.
	 */
	public static SSLAuth getBinding(Class<?> caller) {
		SSLAuth specific = bindings.get(caller);
		return specific != null ? specific : def_auth;
	}
}
