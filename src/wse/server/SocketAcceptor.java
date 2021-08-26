package wse.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import wse.WSE;

/**
 * 
 * Accept loop
 * One instance per port
 * 
 * Sends accepted sockets to receiver's {@link SocketHandler}
 * 
 * @author WSE
 *
 */
public class SocketAcceptor implements Runnable{

	private static final Logger logger = WSE.getLogger();
	
	private final ServerSocket server;
	private final ServiceReceiver receiver;
	private final SessionCounter counter;
	
	private boolean running = false;
	
	public SocketAcceptor(ServiceReceiver receiver, ServerSocket server) {
		this.server = server;
		this.receiver = receiver;
		this.counter = receiver.getSessionCounter();
	}

	@Override
	public void run() {
		running = true;
		while (running)
		{
			try {
				Socket socket = server.accept();
				logger.finest("accept()");

				socket.setTcpNoDelay(true);
				counter.plus();
				InetAddress ia = socket.getInetAddress();
				String address = ia.getHostAddress();
				
				if (receiver.isBanned(address))
				{
					logger.fine("Accepted socket from " + address + ", but IP is Banned!");
					socket.close();
					continue;
				}else {
					logger.fine("Accepted socket from " + address);
				}
				
				socket.setSoTimeout(5000);
				
				Runnable handler = receiver.getSocketHandler(socket);
				
				Thread handle = new Thread(handler);
				handle.start();
				
				logger.finest("Handled by '" + handle.getName() + "'");
				
			}catch(SocketException e)
			{
				// Caused by close()
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				counter.minus();
			}
		}
	}

	public void close()
	{
		running = false;
	}

}
