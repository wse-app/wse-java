package wse.server.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import wse.utils.HttpCodes;
import wse.utils.http.Credentials;
import wse.utils.http.HttpHeader;

public abstract class AuthenticationServlet extends HttpServlet {

	private static Set<Credentials> globallyTrusted = Collections.synchronizedSet(new HashSet<Credentials>());
	private Set<Credentials> trusted = new HashSet<>();
	
	private boolean authorizationEnabled;
	
	public AuthenticationServlet() {
		this(true);
	}

	public AuthenticationServlet(boolean authorizationEnabled) {
		super();
		this.authorizationEnabled = authorizationEnabled;
	}
	
	@Override
	public void doAny(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (authorizationEnabled && !isAuthorized(request, request.getAuthorization())) {
			response.setAttribute("WWW-Authenticate", "Basic realm=\"This is a protected area\"");
			response.sendError(HttpCodes.UNAUTHORIZED);
			return;
		}
		super.doAny(request, response);
	}
	
	public boolean isAuthorized(HttpHeader request, Credentials credentials) {
		return globallyTrusted.contains(credentials) || trusted.contains(credentials);
	}
	
	public void trust(Credentials cred) {
		trusted.add(cred);
	}

	public static void globalTrust(Credentials cred) {
		globallyTrusted.add(cred);
	}
	
	public static boolean isAuthorized(AuthenticationServlet servlet, HttpHeader request, Credentials credentials) {
		return globallyTrusted.contains(credentials) || (servlet != null && servlet.trusted.contains(credentials));
	}

}
