package wse.server.servlet;

import java.io.IOException;

import wse.server.HttpCallTreatment;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;

public abstract class AbstractHttpServlet implements HttpCallTreatment {

	public static void CORSAllowAll(HttpServletResponse response) {
		HttpHeader header = response.getHttpHeader();
		header.setAttribute("Access-Control-Allow-Origin", "*");
		header.setAttribute("Access-Control-Allow-Headers", "*");
		header.setAttribute("Access-Control-Allow-Methods", "*");
	}

	@Override
	public final void treatCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doAny(request, response);
	}

	/**
	 * Called for all Http Methods. Default implementation is a switch on the Http
	 * Method, i.e a GET request is forwarded to the doGet method.
	 * 
	 * 
	 * @param request  The request object. Contains information about the http
	 *                 request.
	 * @param response The response object. Contains logic for making the http
	 *                 response.
	 * @throws IOException
	 * 
	 * @see {@link #doGet(HttpServletRequest, HttpServletResponse)}
	 * @see {@link wse.utils.http.HttpMethod}
	 */
	public void doAny(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpMethod method = request.getMethod();
		switch (method) {
		case GET:
			doGet(request, response);
			break;
		case HEAD:
			doHead(request, response);
			break;
		case OPTIONS:
			doOptions(request, response);
			break;
		case PATCH:
			doPatch(request, response);
			break;
		case POST:
			doPost(request, response);
			break;
		case PUT:
			doPut(request, response);
			break;
		case DELETE:
			doDelete(request, response);
			break;
		case SECURE:
			doSecure(request, response);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * Method for handling an http transaction. Called by doAny when the http method
	 * is GET.
	 * 
	 * @param request  The request object. Contains information about the http
	 *                 request.
	 * @param response The response object. Contains logic for making the http
	 *                 response.
	 * @throws IOException
	 * @see {@link #doAny(HttpServletRequest, HttpServletResponse)}
	 */
	public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public abstract void doSecure(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
