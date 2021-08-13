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
import wse.utils.exception.WseBuildingException;
import wse.utils.exception.WseHttpException;
import wse.utils.exception.WseParsingException;
import wse.utils.exception.WseWebSocketException;
import wse.utils.http.HttpBuilder;
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

	private static Object lock = new Object();
	protected static int threadsActive = 0;

	private static final Logger logger = WSE.getLogger();

	private final Socket socket;
	private final ServerSocket serverSocket;
	private final CallTreatment treatment;

	protected SocketHandler(final Socket socket, final ServerSocket serverSocket, final CallTreatment treatment) {
		this.socket = socket;
		this.serverSocket = serverSocket;
		this.treatment = treatment;
	}

	@Override
	public void run() {

		onTreatmentStart();

		try {

			socket.setSoTimeout(60 * 1000);
			final InputStream input = socket.getInputStream();

			HttpResult file = HttpBuilder.read(input, true);
			if (file == null)
				return;
			RequestInfo info = RequestInfo.fromSocket(socket, serverSocket);

			OutputStream output = new ProtectedOutputStream(socket.getOutputStream());
			if (file.getHeader().getMethod() != HttpMethod.SECURE) {
				output = new RecordingOutputStream(output, logger, Level.FINEST, "Response:");				
			}
			HttpServletResponse response = new HttpServletResponse(output);

			try {
				// TODO implement connection: keep-alive
				HttpServletRequest request;
				InputStream content = file.getContent();

				if (file.getHeader().getMethod() != HttpMethod.SECURE) {
					
					long len = file.getHeader().getContentLength();

					if (len >= 0 && len <= 40000)
						content = new RecordingInputStream(content, logger, Level.FINEST, "Request Body: ");
				}

				request = HttpServletRequest.make(file.getHeader(), info, new ProtectedInputStream(content));
				if (logger.isLoggable(Level.FINER)) {
					byte[] header = file.getHeader().toByteArray();
					logger.finer("Request Header: [" + (header.length) + " bytes]\n" + new String(header));
				}
				

				treatment.treatCall(request, response);

				output.flush();
				output.close();
				response.close();
			} catch (WseHttpException e) {
				Integer sendCode = e.getStatusCode();
				response.sendError(sendCode != null ? sendCode : HttpCodes.BAD_REQUEST, e.getMessage());
				logger.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage(), e);
			} catch (WseParsingException e) {
				response.sendError(HttpCodes.BAD_REQUEST, e.getMessage());
				logger.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage(), e);
			} catch (WseBuildingException e) {
				response.sendError(HttpCodes.INTERNAL_SERVER_ERROR, "Server failed to produce a correct soap response");
				logger.log(Level.SEVERE, "Server failed to produce a correct soap response: " + e.getMessage(), e);
			} catch (SocketException | WseWebSocketException e) {
				logger.log(Level.FINER, e.getClass().getName() + ": " + e.getMessage(), e);
			} catch (Exception e) {
				logger.log(Level.FINE, "Unknown error: " + e.getMessage(), e);
				if (!socket.isClosed()) {
					
					response.sendError(HttpCodes.INTERNAL_SERVER_ERROR);
				}
			}
		} catch (SocketException e) {
			logger.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage());
		} catch (IOException e) {
			logger.log(Level.FINER, e.getClass().getName() + ": " + e.getMessage());
		} catch (Exception e) {
			logger.log(Level.INFO, "Unhandled " + e.getClass().getName() + ", discarding request: " + e.getMessage(),
					e);
		} finally {
			try {
				if (!socket.isClosed())
					socket.close();
			} catch (Exception e) {
			}
		}

		onTreatmentEnd();
	}

	private void onTreatmentStart() {
		synchronized (lock) {
			threadsActive++;
		}
	}

	private void onTreatmentEnd() {
		synchronized (lock) {
			threadsActive--;
		}
	}
}
