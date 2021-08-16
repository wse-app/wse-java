package wse.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import wse.WSE;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.server.servlet.OperationListener;
import wse.server.servlet.soap.OperationServlet;
import wse.utils.HttpCodes;
import wse.utils.exception.WseException;

public final class ServiceManager implements CallTreatment {
	public static final String ActionAttribName = "SOAPAction";

	private Treatment defaultTreatment;
	private static final Logger logger = WSE.getLogger();

	private final Map<String, Treatment> path_servlet;
	private final Map<Pattern, Treatment> pattern_servlet;

	protected ServiceManager() {
		path_servlet = new HashMap<>();
		pattern_servlet = new HashMap<>();
	}

	public void register(Class<? extends OperationListener> listener) {
		OperationServlet servlet;
		try {
			servlet = new OperationServlet(listener);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new WseException(e);
		}
		register(servlet.getRequestedPath(), servlet);
	}

	public void register(OperationListener listener) {
		OperationServlet servlet = new OperationServlet(listener);
		register(servlet.getRequestedPath(), servlet);
	}

	public void registerDefault(CallTreatment treatment) {
		registerDefault(new Treatment(treatment));
	}

	public void registerDefault(Class<? extends CallTreatment> treatment) {
		registerDefault(new Treatment(treatment));
	}

	public void registerDefault(Treatment treatment) {
		if (defaultTreatment != null && treatment != null)
			logger.warning("Servlet \"" + defaultTreatment.getTreatmentClassName()
					+ "\" already registered as default, OVERRIDING!");

		if (treatment != null) {
			logger.info("Servlet \"" + treatment.getTreatmentClassName() + "\" set as default.");
		} else if (this.defaultTreatment != null) {
			logger.info("Default treatment removed");
		}

		this.defaultTreatment = treatment;
	}

	public void register(String path, CallTreatment treatment) {
		register(path, new Treatment(treatment));
	}

	public void register(String path, Class<? extends CallTreatment> treatment) {
		register(path, new Treatment(treatment));
	}

	public void register(String path, Treatment treatment) {
		if (treatment == null)
			throw new NullPointerException("null Treatment");

		if (path == null) {
			registerDefault(treatment);
			return;
		}

		path = path.trim();

		if (path.isEmpty())
			path = "/";

		if (path_servlet.containsKey(path)) {
			Treatment registered = path_servlet.get(path);
			logger.warning("Service \"" + registered.getTreatmentClassName() + "\" already registered at path: " + path
					+ ", OVERRIDING!");
		}

		path_servlet.put(path, treatment);
		logger.info("Servlet \"" + treatment.getTreatmentClassName() + "\" bound to \"" + path + "\"");
		return;
	}

	public void register(Pattern pattern, CallTreatment treatment) {
		register(pattern, new Treatment(treatment));
	}

	public void register(Pattern pattern, Class<? extends CallTreatment> treatment) {
		register(pattern, new Treatment(treatment));
	}

	public void register(Pattern pattern, Treatment treatment) {
		if (treatment == null)
			throw new NullPointerException("null Treatment");

		if (pattern == null) {
			registerDefault(treatment);
			return;
		}

		if (pattern_servlet.containsKey(pattern)) {
			Treatment registered = pattern_servlet.get(pattern);
			logger.warning("Service \"" + registered.getTreatmentClassName() + "\" already registered at path: "
					+ pattern.pattern() + ", OVERRIDING!");
		}

		pattern_servlet.put(pattern, treatment);
		logger.info("Servlet \"" + treatment.getTreatmentClassName() + "\" bound to \"" + pattern.pattern() + "\"");
		return;
	}

	private Treatment getService(String path) {
		Treatment treatment = path_servlet.get(path);
		if (treatment != null)
			return treatment;

		for (Entry<Pattern, Treatment> entry : pattern_servlet.entrySet()) {
			if (entry.getKey().matcher(path).matches()) {
				return entry.getValue();
			}
		}

		return null;
	}

	@Override
	public void treatCall(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestPath();
		if (path == null) {
			response.sendError(HttpCodes.BAD_REQUEST, "Invalid http request line");
			return;
		}

		// TODO Should be taken over by a HttpServlet

		Treatment treatment = getService(path);

		if (treatment == null)
			treatment = this.defaultTreatment;

		if (treatment == null) { // default might be null
			// Bad URI or Service doesen't exist.
			logger.info("Received call for \"" + request.getRequestURI().toString()
					+ "\", but no treatment was available for this path");
			response.sendError(HttpCodes.NOT_FOUND, "Bad URI or Service doesnt exist");
			return;
		}

		logger.info("Received call for \"" + request.getRequestURI().toString() + "\", covered by "
				+ treatment.getTreatmentClassName());

		CallTreatment servlet = treatment.getCallTreatment();
		if (servlet == null) {
			logger.severe("CallTreatment for " + request.getRequestURI().toString()
					+ " was registered but could not be instanced");
			response.sendError(HttpCodes.NOT_FOUND, "Bad URI or Service doesnt exist");
			return;
		}

		// Service exists!
		servlet.treatCall(request, response);
	}

}
