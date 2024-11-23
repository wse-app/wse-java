package wse.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import wse.utils.Protocol;

public class PersistantConnectionStore {

	private static final Object LOCK = new Object();
	private static final Map<String, LinkedList<PersistantConnection>> CONNECTIONS = new HashMap<>();

	public static IOConnection useConnection(Protocol protocol, String host, int port) {
		String key = key(protocol, host, port);

		if (!CONNECTIONS.containsKey(key))
			return null;

		synchronized (LOCK) {
			LinkedList<PersistantConnection> connections = CONNECTIONS.get(key);

			if (connections == null || connections.size() == 0)
				return null;

			while (connections.size() > 0) {
				PersistantConnection pc = connections.removeLast();
				try {
					if (pc.isValid())
						return pc;
				} catch (IOException ignore) {
				}
			}
			return null;
		}
	}

	public static void storeConnection(Protocol protocol, String host, int port, Integer timeout, Integer max,
			IOConnection connection) {
		String key = key(protocol, host, port);

		PersistantConnection pc;
		if (connection instanceof PersistantConnection) {
			pc = (PersistantConnection) connection;
		} else {
			pc = new PersistantConnection(connection, timeout, max);
		}

		pc.count++;

		synchronized (LOCK) {
			LinkedList<PersistantConnection> connections = CONNECTIONS.get(key);

			if (connections == null) {
				connections = new LinkedList<>();
				CONNECTIONS.put(key, connections);
			}

			connections.addLast(pc);
		}
	}

	private static String key(Protocol protocol, String host, int port) {
		return String.format("%s_%s_%d", protocol, host, port);
	}

}
