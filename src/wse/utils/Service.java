package wse.utils;

import wse.server.listener.ServiceListener;
import wse.utils.ssl.SSLAuth;

public class Service<T extends HttpCall> extends HttpCall {
	public Class<? extends ServiceListener<? extends ComplexType, ? extends ComplexType>> listener;
	public Class<T> caller;

	/**
	 * Service binding
	 */
	private SSLAuth sslstore;

	private final HttpCall default_call;

	private ServiceListener<? extends ComplexType, ? extends ComplexType> default_listener;

	public boolean use_fresh_listener = false; // Use fresh service for each call, not recommended

	public Service(Class<T> caller) {
		this(caller, null);
	}

	public Service(Class<T> caller,
			Class<? extends ServiceListener<? extends ComplexType, ? extends ComplexType>> listener) {
		if (caller == null)
			throw new IllegalArgumentException("Caller can not be null");

		this.listener = listener;
		this.caller = caller;

		default_call = makeCall();

		this.setTarget(default_call.getTarget());
	}

	public void bindToSSLStore(SSLAuth sslStore) {
		this.sslstore = sslStore;
	}

	public SSLAuth getSSLStore() {
		return this.sslstore;
	}

	public T makeCall() {
		T newCall;
		try {
			newCall = caller.newInstance();
			newCall.bindToSSLStore(this.sslstore);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}

		if (default_call != null) {
			newCall.setTarget(this.getTarget());
		}
		return newCall;
	}

	public ServiceListener<? extends ComplexType, ? extends ComplexType> makeListener() {
		if (listener != null) {
			try {
				ServiceListener<? extends ComplexType, ? extends ComplexType> newListener = listener.newInstance();
				return newListener;
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public ServiceListener<? extends ComplexType, ? extends ComplexType> getListener() {
		if (listener == null)
			return null;

		if (default_listener == null || use_fresh_listener) {
			return makeListener();
		}
		return default_listener;
	}

	@Override
	public String getServiceName() {
		return default_call.getServiceName();
	}

	@Override
	public String getSoapAction() {
		return default_call.getSoapAction();
	}

	public Class<? extends HttpCall> getServiceClass() {
		return default_call.getClass();
	}
}
