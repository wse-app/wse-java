package wse.server.servlet.soap;

import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;

public abstract class OperationListener {

	protected HttpServletRequest request;
	protected HttpServletResponse response;

	public abstract String path();

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}
}
