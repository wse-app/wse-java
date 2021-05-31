package wse.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

public class RequestInfo
{
	private InetAddress inetAddress;
	private int fromPort;
	private boolean usingSSL;
	
	private int reachPort;
	
	
	private RequestInfo() { }
	
	public static final RequestInfo fromSocket(Socket socket, ServerSocket server)
	{
		RequestInfo result = new RequestInfo();
		
		result.inetAddress = socket.getInetAddress();
		result.fromPort = socket.getPort();
		result.usingSSL = (socket instanceof SSLSocket);
		
		result.reachPort = server.getLocalPort();
		
		return result;
	}
	
	public String toString()
	{
		return inetAddress.getHostAddress() + ":" + fromPort + "|SSL=" + usingSSL;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public int getFromPort() {
		return fromPort;
	}

	public boolean isUsingSSL() {
		return usingSSL;
	}
	
	public int getReachPort() {
		return reachPort;
	}
	
}
