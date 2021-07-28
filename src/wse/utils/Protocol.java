package wse.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class Protocol {
	private static final LinkedList<Protocol> values = new LinkedList<>();

	public static final SecureProtocol WEB_SOCKET_SECURE = new SecureProtocol("wss");
	public static final Protocol WEB_SOCKET = new Protocol("ws");
	public static final SecureProtocol SHTTP = new SecureProtocol("shttp");
	public static final SecureProtocol HTTPS = new SecureProtocol("https");
	public static final Protocol HTTP = new Protocol("http");

//	HTTP("http"), 
//	HTTPS("https"), 
//	SHTTP("shttp"), 
//	WEB_SOCKET("ws"), 
//	WEB_SOCKET_SECURE("wss");

	private String nicename;

	public Protocol(String nicename) {
		this.nicename = nicename;
		values.addFirst(this);
	}

	public String toString() {
		return nicename;
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
		return Collections.unmodifiableCollection(values);
	}

	public static Protocol parseIgnoreCase(String nicename) {
		for (Protocol t : Protocol.values()) {
			if (t.nicename.equalsIgnoreCase(nicename))
				return t;
		}
		return null;
	}

	public static Protocol parse(String nicename) {
		for (Protocol t : Protocol.values()) {
			if (t.nicename.equals(nicename))
				return t;
		}
		return null;
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
