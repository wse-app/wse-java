package wse.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import wse.WSE;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;

public abstract class ServiceReceiver implements HttpCallTreatment {

	protected Thread listenerThread;
	private final int port;
	private final ServiceManager manager;
	private final SessionCounter counter;

	private SocketAcceptor acceptor;
	private Restrictions restrictions;

	private final Set<String> banned;
	
	protected final static Logger log = WSE.getLogger();

	public ServiceReceiver(ServiceManager manager, int port, Restrictions restrictions) {
		this.port = port;
		this.manager = manager;
		this.banned = new HashSet<String>();
		this.counter = new SessionCounter();
		this.restrictions = restrictions;
	}

	public void start() throws IOException {
		initSocket();

		SocketAcceptor acceptor = getSocketAcceptor();
		
		this.listenerThread = new Thread(acceptor, getProtocol() + ":" + this.port);
		this.listenerThread.start();
		
		log.info(getProtocol() + " listener started on " + getServerSocket().getInetAddress().toString() + ":" + port);
	}

	protected abstract void initSocket() throws IOException;
	protected SocketAcceptor getSocketAcceptor()
	{
		if (this.acceptor == null)
			this.acceptor =  new SocketAcceptor(this, getServerSocket());		
		return this.acceptor;
	}
	protected Runnable getSocketHandler(Socket socket)
	{
		return new SocketHandler(socket, getServerSocket(), this);
	}
	
	public abstract String getProtocol();

	public void close() throws IOException {
		if (this.acceptor != null)
			this.acceptor.close();
		getServerSocket().close();
	}

	public abstract ServerSocket getServerSocket();

	public Restrictions getRestrictions() {
		if (this.restrictions == null)
			this.restrictions = new Restrictions();
		return this.restrictions;
	}
	
	protected static ServerSocket makeSocket(int port, Restrictions r, ServerSocketFactory factory) throws UnknownHostException, IOException
	{
		ServerSocket socket = factory.createServerSocket();
		
		socket.setReuseAddress(true);
		
		switch(r.getAcceptPolicy())
		{
		case LOCAL:
			socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port), r.getBacklog());
			break;
		case LAN:
			if (r.isSingleAccept()) {
				socket.bind(new InetSocketAddress(InetAddress.getByName(r.getAcceptHost()), port), r.getBacklog());
				break;
			}
			// fall through
		default:
			socket.bind(new InetSocketAddress(port), r.getBacklog());
			break;
		}
		
		return socket;
	}

	public long getActiveSessions() {
		return counter.get();
	}

	public SessionCounter getSessionCounter() {
		return counter;
	}

	public int getPort() {
		return this.port;
	}

	public Thread getListenerThread() {
		return listenerThread;
	}

	public void banHost(String host) {
		banned.add(host);
	}

	public boolean unbanHost(String host) {
		return banned.remove(host);
	}

	public boolean isBanned(String host) {
		return this.banned.contains(host);
	}

	public void treatCall(HttpServletRequest request, HttpServletResponse response)  throws IOException{
		manager.treatCall(request, response);
	}
}
