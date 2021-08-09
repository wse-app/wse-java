package wse.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Protocol {
	private static final Map<String, Protocol> values = new LinkedHashMap<>();

	public static final SecureProtocol WEB_SOCKET_SECURE = new SecureProtocol("wss");
	public static final Protocol WEB_SOCKET = new Protocol("ws");
	public static final SecureProtocol SHTTP = new SecureProtocol("shttp");
	public static final SecureProtocol HTTPS = new SecureProtocol("https");
	public static final Protocol HTTP = new Protocol("http");
	
	public static void register(Protocol protocol) {
		register(protocol.name, protocol);
	}
	
	public static void register(String name, Protocol protocol) {
		if (name == null)
			throw new NullPointerException("name == null");
		if (protocol == null)
			throw new NullPointerException("protocol == null");
		
		name = name.toLowerCase();
		values.put(name, protocol);
	}
	
	static {
		register(WEB_SOCKET_SECURE);
		register(WEB_SOCKET);
		register(SHTTP);
		register(HTTPS);
		register(HTTP);
	}

	private final String name;

	/**
	 * @param name Will be used when parsing an URL to find the protocol.
	 */
	public Protocol(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public boolean isWebSocket() {
		return this == WEB_SOCKET || this == WEB_SOCKET_SECURE;
	}

	public boolean isSecure() {
		return this == HTTPS || this == SHTTP || this == WEB_SOCKET_SECURE;
	}

	public Integer getDefaultPort() {
		if (this == HTTP)
			return 80;
		if (this == HTTPS)
			return 443;
		return null;
	}

	public static Collection<Protocol> values() {
		return Collections.unmodifiableCollection(values.values());
	}


	public static Protocol forName(String name) {
		if (name == null) return null;
		name = name.toLowerCase();
		return values.get(name);
	}

	/**
	 * Gets the default SocketFactory for this protocol.
	 * 
	 * @return the default SocketFactory
	 */
	public SocketFactory getDefaultSocketFactory() {
		return SocketFactory.getDefault();
	}

	public static class SecureProtocol extends Protocol {
		public SecureProtocol(String nicename) {
			super(nicename);
		}

		/**
		 * @return the default SSLSocketFactory
		 */
		public SSLSocketFactory getDefaultSocketFactory() {
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		}
	}
}
