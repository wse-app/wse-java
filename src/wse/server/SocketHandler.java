package wse.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.utils.HttpCodes;
import wse.utils.HttpResult;
import wse.utils.exception.WseHttpException;
import wse.utils.exception.WseWebSocketException;
import wse.utils.http.HttpBuilder;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.stream.ProtectedInputStream;
import wse.utils.stream.ProtectedOutputStream;
import wse.utils.stream.RecordingInputStream;
import wse.utils.stream.RecordingOutputStream;

/**
 * Handles an already accepted socket received from {@link SocketAcceptor}
 * 
 * @author WSE
 *
 */
public final class SocketHandler implements Runnable {

	protected static int handlersActive = 0;

	private static final Object LOCK = new Object();
	private static final Logger LOG = WSE.getLogger();
	
	/** Persistant socket timeout in milliseconds */
	public static int PERSISTANT_SOCKET_TIMEOUT = 60000;

	private final Socket socket;
	private final ServerSocket serverSocket;
	private final HttpCallTreatment treatment;

	protected SocketHandler(final Socket socket, final ServerSocket serverSocket, final HttpCallTreatment treatment) {
		this.socket = socket;
		this.serverSocket = serverSocket;
		this.treatment = treatment;
	}

	@Override
	public void run() {

		onTreatmentStart();

		try {
			socket.setSoTimeout(5 * 1000);

			InputStream input = new ProtectedInputStream(socket.getInputStream());
			OutputStream output = new ProtectedOutputStream(socket.getOutputStream());

			readLoop(input, output);

		} catch (SocketException | WseWebSocketException e) {
			LOG.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage());
		} catch (IOException e) {
			LOG.log(Level.FINER, e.getClass().getName() + ": " + e.getMessage());
		} catch (Throwable e) {
			LOG.log(Level.INFO, "Unhandled " + e.getClass().getName() + ", discarding request: " + e.getMessage(), e);
		}

		try {
			if (!socket.isClosed())
				socket.close();
		} catch (Throwable ignore) {
		}

		onTreatmentEnd();
	}

	private void readLoop(InputStream input, OutputStream output) throws Throwable {
		int count = 0;
		for (;;) {
			boolean keepAlive = readHttp(count, input, output);
			if (!keepAlive)
				break;
			
			count++;
			LOG.fine("Persistant socket, count=" + count);
		}
	}

	private boolean readHttp(int count, InputStream input, OutputStream output) throws Throwable {
		HttpResult file = HttpBuilder.read(input, true);

		if (count > 0)
			LOG.fine("Got new message on persistant socket");

		RequestInfo info = RequestInfo.fromSocket(socket, serverSocket);
		HttpHeader requestHeader = file.getHeader();

		if (requestHeader.getMethod() != HttpMethod.SECURE) {
			output = new RecordingOutputStream(output, LOG, Level.FINEST, "Response:");
		}
		HttpServletResponse response = new HttpServletResponse(output);
		HttpHeader responseHeader = response.getHttpHeader();

		boolean keepAlive = requestHeader.getConnection(/* lowerCase: */ true).contains("keep-alive");

		if (keepAlive) {
			responseHeader.setConnection("keep-alive");
			
			if (PERSISTANT_SOCKET_TIMEOUT > 0) {
				// Keep-Alive timeout is in seconds
				responseHeader.setKeepAlive(PERSISTANT_SOCKET_TIMEOUT / 1000, null);
			}
		}

		try {

			HttpServletRequest request;
			InputStream content = file.getContent();

			if (requestHeader.getMethod() != HttpMethod.SECURE) {

				long len = requestHeader.getContentLength();

				if (len >= 0 && len <= 40000)
					content = new RecordingInputStream(content, LOG, Level.FINEST, "Request Body: ");
			}

			request = HttpServletRequest.make(file.getHeader(), info, content);
			if (LOG.isLoggable(Level.FINER)) {
				byte[] header = requestHeader.toByteArray();
				LOG.finer("Request Header: [" + (header.length) + " bytes]\n" + new String(header));
			}

			treatment.treatCall(request, response);

			if (!response.isHeaderWritten()) {
				response.setContentLength(0);
				response.writeHeader();
			}

			keepAlive = requestHeader.getConnection(/* lowerCase: */ true).contains("keep-alive");

			output.flush();

		} catch (WseHttpException e) {
			Integer sendCode = e.getStatusCode();
			response.sendError(sendCode != null ? sendCode : HttpCodes.BAD_REQUEST, e.getMessage());
			LOG.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage(), e);
		}
//		catch (WseParsingException e) {
//			response.sendError(HttpCodes.BAD_REQUEST, e.getMessage());
//			logger.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage(), e);
//		} catch (WseBuildingException e) {
//			response.sendError(HttpCodes.INTERNAL_SERVER_ERROR, "Server failed to produce a correct soap response");
//			logger.log(Level.SEVERE, "Server failed to produce a correct soap response: " + e.getMessage(), e);
//		}

		if (keepAlive) {
			if (input.available() > 0) {
				LOG.severe("Connection: keep-alive, but input contains left-over data. Persistant socket may fail.");
			}
			
			socket.setSoTimeout(PERSISTANT_SOCKET_TIMEOUT);
		}

		return keepAlive;
	}

	private void onTreatmentStart() {
		synchronized (LOCK) {
			handlersActive++;
		}
	}

	private void onTreatmentEnd() {
		synchronized (LOCK) {
			handlersActive--;
		}
	}
}
