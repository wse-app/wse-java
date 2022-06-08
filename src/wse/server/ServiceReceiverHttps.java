package wse.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import wse.WSE;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.server.shttp.SHttpServerSessionStore;
import wse.utils.HttpCodes;
import wse.utils.SHttp;
import wse.utils.http.HttpMethod;
import wse.utils.shttp.SKey;
import wse.utils.ssl.SSLAuth;

public class ServiceReceiverHttps extends ServiceReceiver {

	private SSLAuth auth;
	private SSLServerSocket socket;

	private boolean acceptHttps = true;
	private boolean acceptShttp = false;

	private SHttpServerSessionStore store;

	private Random random = new Random();
	private List<Integer> shttpRedirect = new ArrayList<>();

	public static final String METHOD_PATTERN = "AES[0-9][0-9][0-9]?";
	private static final String DEFAULT_METHOD = "AES128";

	public ServiceReceiverHttps(ServiceManager manager, int port, Restrictions restrictions) {
		super(manager, port, restrictions);
	}

	@Override
	protected void initSocket() throws IOException {

		SSLServerSocketFactory factory;
		if (auth != null) {
			factory = auth.getSSLServerSocketFactory();
		} else {
			factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		}

		Restrictions r = getRestrictions();

		socket = (SSLServerSocket) ServiceReceiver.makeSocket(getPort(), r, factory);

		if (r.getNeedClientAuth())
			socket.setNeedClientAuth(true);
		socket.setReuseAddress(true);
		socket.setSoTimeout(0);
	}

	public SSLServerSocket getServerSocket() {
		return socket;
	}

	public void bindToSSLStore(SSLAuth auth) {
		this.auth = auth;
	}

	public SSLAuth getSSLAuth() {
		return auth;
	}

	public void setAcceptHttps(boolean acceptHttps) {
		this.acceptHttps = acceptHttps;
	}

	public void setAcceptSHttp(boolean acceptSHttp) {
		if (!this.acceptShttp && acceptSHttp && store == null) {
			store = new SHttpServerSessionStore();
		}
		this.acceptShttp = acceptSHttp;
	}

	public void addAvailableSHttpPort(int port) {
		shttpRedirect.add(port);
	}

	public SHttpServerSessionStore getSHttpSessionStore() {
		return store;
	}

	@Override
	public String getProtocol() {
		Restrictions r = getRestrictions();
		return "Https/" + (r.getNeedClientAuth() ? "cc" : "anon");
	}

	@Override
	public void treatCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getMethod() == HttpMethod.GET && SHttp.INIT_PATH.equals(request.getRequestPath())) {

			if (acceptShttp) {
				treatSHttpInit(request, response);
				return;
			}

			response.sendError(HttpCodes.FORBIDDEN, "sHttp not supported on this location");
			return;
		}

		if (acceptHttps) {
			super.treatCall(request, response);
			return;
		}

		response.sendMethodNotAllowed(HttpMethod.GET, HttpMethod.SECURE);
	}

	private int getRandomSHttpRedirectPort() {
		if (shttpRedirect.size() == 0)
			return -1;
		if (shttpRedirect.size() == 1)
			return shttpRedirect.get(0);

		return shttpRedirect.get(random.nextInt(shttpRedirect.size()));
	}

	/**
	 * Initialize shttp
	 */
	public void treatSHttpInit(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String path = request.getRequestPath();
		if (!SHttp.INIT_PATH.equals(path)) {
			response.sendError(HttpCodes.BAD_REQUEST, "Invalid sHttp init path");
			return;
		}

		String method = DEFAULT_METHOD;

		if (!method.matches(METHOD_PATTERN)) {
			response.sendError(HttpCodes.BAD_REQUEST,
					"Encryption method not supported, supported: AESx, x=[" + SHttp.getKeyLengthsSupported() + "]");
			return; // Invalid method
		}

		int keyLen;
		try {
			keyLen = Integer.parseInt(method.substring(3));
		} catch (Exception e) {
			response.sendError(HttpCodes.BAD_REQUEST,
					"Encryption method not supported, supported: AESx, x=[" + SHttp.getKeyLengthsSupported() + "]");
			return; // Invalid method
		}

		if (!SHttp.keyLengthSupported(keyLen) || (keyLen % 8 != 0)) {
			// Key length not supported
			response.sendError(HttpCodes.BAD_REQUEST,
					"Key length not supported, supported: [" + SHttp.getKeyLengthsSupported() + "]");
			return;
		}

		SKey key = SHttpServerSessionStore.generateKey(keyLen);
		store.storeKey(key);

		int redirectPort = getRandomSHttpRedirectPort();

		StringBuilder content = new StringBuilder(200);
		content.append(key.getKeyName()).append(' ');
		content.append(WSE.printBase64Binary(key.getKey())).append(' ');
		content.append(redirectPort).append(' ');
		content.append(key.getLifeLength()).append(' ');
		content.append(key.getKey().length * 8);

		byte[] data = content.toString().getBytes();

		response.setContentLength(data.length);
		response.write(data);
		return;
	}

}
