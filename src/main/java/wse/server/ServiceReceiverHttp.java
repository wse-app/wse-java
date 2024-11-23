package wse.server;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

import wse.utils.Protocol;

public class ServiceReceiverHttp extends ServiceReceiver {

	private ServerSocketFactory factory;
	private ServerSocket socket;

	public ServiceReceiverHttp(ServiceManager manager, int port, Restrictions restrictions) {
		super(manager, port, restrictions);

		factory = ServerSocketFactory.getDefault();
	}

	@Override
	protected void initSocket() throws IOException {
		socket = ServiceReceiver.makeSocket(getPort(), getRestrictions(), factory);

		socket.setReuseAddress(true);
		socket.setSoTimeout(0);
	}

	@Override
	public ServerSocket getServerSocket() {
		return socket;
	}

	@Override
	public String getProtocol() {
		return Protocol.HTTP.toString();
	}
}
