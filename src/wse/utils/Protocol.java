package wse.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Protocol {
	private static final Map<String, Protocol> values = new LinkedHashMap<>();

	public static final Protocol WEB_SOCKET_SECURE = new Protocol("wss", true);
	public static final Protocol WEB_SOCKET = new Protocol("ws", false);
	public static final Protocol SHTTP = new Protocol("shttp", true);
	public static final Protocol HTTPS = new Protocol("https", true);
	public static final Protocol HTTP = new Protocol("http", false);

	public static void register(Protocol protocol) {
		register(protocol.name, protocol);
	}

	public static void register(String name, Protocol protocol) {
		Objects.requireNonNull(name, "name == null");
		Objects.requireNonNull(protocol, "protocol == null");

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
	private final boolean secure;

	/**
	 * @param name Will be used when parsing an URL to find the protocol.
	 */
	public Protocol(String name, boolean secure) {
		this.name = name;
		this.secure = secure;
	}

	public String toString() {
		return name;
	}

	public boolean isWebSocket() {
		return this == WEB_SOCKET || this == WEB_SOCKET_SECURE;
	}
	
	/**
	 * Return true if connections made with this protocol should consider being stored as persistent connections.
	 * 
	 * WebSocket connections are not considered persistent in that they are not shared by multiple Calls/WebSockets.
	 * 
	 * SHTTP connections Should NOT be persistent, because this would be against the nature of the protocol.
	 * @return
	 */
	public boolean supportPersistentConnection() {
		return this == HTTPS || this == HTTP;
	}

	public boolean isSecure() {
		return secure;
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
		if (name == null)
			return null;
		name = name.toLowerCase();
		return values.get(name);
	}

}
