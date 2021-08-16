package wse.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.shttp.SHttpSessionStore;
import wse.utils.exception.WseException;
import wse.utils.ssl.SSLAuth;

public class WSEServer {
	private ServiceManager callManager;

	private static Logger logger = WSE.getLogger();

	private Map<Integer, ServiceReceiver> listeners = new HashMap<>();
	private boolean running = false;

	public WSEServer() {
		this.callManager = new ServiceManager();
	}

	public Restrictions addHttp(int port) {
		if (!available(port) || listeners.containsKey(port)) {
			logger.severe("Port " + port + " is already in use");
			return null;
		}

		Restrictions restrictions = new Restrictions();
		ServiceReceiver receiver = new ServiceReceiverHttp(this.callManager, port, restrictions);

		listeners.put(port, receiver);

		return restrictions;
	}

	public Restrictions addHttps(int port, SSLAuth auth) {

		ServiceReceiver r;
		ServiceReceiverHttps p = null;

		if (listeners.containsKey(port)) {

			if ((r = this.getReceiverByPort(port)) instanceof ServiceReceiverHttps) {
				p = (ServiceReceiverHttps) r;
				
			} else {
				logger.severe("Port " + port + " is already in use");
				return null;
			}
		} else if (!available(port)) {
			logger.severe("Port " + port + " is already in use");
			return null;
		}

		if (p != null) {

			if (auth != null && (auth != p.getSSLAuth())) {
				logger.severe("Can't have more than one SSLAuth on one port. (" + p.getProtocol() + ")");
				return null;
			}

			p.setAcceptHttps(true);
			return p.getRestrictions();
		} else {
			ServiceReceiverHttps receiver = new ServiceReceiverHttps(this.callManager, port, new Restrictions());
			receiver.bindToSSLStore(auth);

			listeners.put(port, receiver);

			return receiver.getRestrictions();
		}

	}

	public Restrictions addSHttp(int https_port, SSLAuth auth, int shttp_port) {
		ServiceReceiver r;
		ServiceReceiverHttps initializer = null;

		if (listeners.containsKey(https_port)) {

			if ((r = this.getReceiverByPort(https_port)) instanceof ServiceReceiverHttps) {
				initializer = (ServiceReceiverHttps) r;
			} else {
				logger.severe("Port " + https_port + " is already in use");
				return null;
			}
		} else if (!available(shttp_port)) {
			logger.severe("Port " + https_port + " is already in use");
			return null;
		}

		if (!available(shttp_port) || listeners.containsKey(shttp_port)) {
			logger.severe("Port " + shttp_port + " is already in use");
			return null;
		}

		Restrictions restrictions = new Restrictions(); // Should be used by both if https does not exist already

		if (initializer == null) {
			initializer = new ServiceReceiverHttps(this.callManager, https_port, restrictions);
			initializer.setAcceptSHttp(true);
			initializer.setAcceptHttps(false);
			initializer.bindToSSLStore(auth);
			listeners.put(https_port, initializer);
		} else {

			if (auth != null && (auth != initializer.getSSLAuth()) && initializer.getSSLAuth() != null) {
				logger.severe("Can't have more than one SSLAuth on one port. (" + initializer.getProtocol() + ")");
				return null;
			}
			
			if (initializer.getSSLAuth() == null)
				initializer.bindToSSLStore(auth);
			initializer.setAcceptSHttp(true);
		}

		ServiceReceiverSHttp receiver = new ServiceReceiverSHttp(this.callManager, shttp_port, restrictions);
		receiver.setSHttpSessionStore(initializer.getSHttpSessionStore());
		
		initializer.addAvailableSHttpPort(shttp_port);
		listeners.put(shttp_port, receiver);

		return restrictions;
	}

	public ServiceReceiver getReceiverByPort(int port) {
		return listeners.get(port);
	}
	
	public SHttpSessionStore getSHttpSessionStore(int port) {
		ServiceReceiver sr = getReceiverByPort(port);
		
		if (sr instanceof ServiceReceiverHttps)
			return ((ServiceReceiverHttps) sr).getSHttpSessionStore();
		if (sr instanceof ServiceReceiverSHttp)
			return ((ServiceReceiverSHttp) sr).getSHttpSessionStore();
		return null;
	}

	public void start() {
		if (running)
			throw new WseException("Server can't be initialized twice!");
		running = true;

		for (ServiceReceiver r : listeners.values()) {
			try {
				r.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		if (!running)
			throw new WseException("Server is not running");

		for (ServiceReceiver r : listeners.values()) {
			try {
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public ServiceManager getServiceManager() {
		return callManager;
	}

	public static int getSessionsActive() {
		return SocketHandler.threadsActive;
	}

	private final static int MIN_PORT_NUMBER = 0, MAX_PORT_NUMBER = 65535;

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	public static boolean available(int port) {
		if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}
}
