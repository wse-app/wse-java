package wse.server;

import java.io.IOException;

import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;

public interface HttpCallTreatment {
	/**
	 * Method for handling an http transaction.
	 * 
	 * @param request  The request object. Contains information about the http
	 *                 request.
	 * @param response The response object. Contains logic for making the http
	 *                 response.
	 * @throws IOException
	 */
	void treatCall(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
