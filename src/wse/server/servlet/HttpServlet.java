package wse.server.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import wse.WSE;
import wse.utils.ArrayUtils;
import wse.utils.ClassUtils;
import wse.utils.HttpCodes;
import wse.utils.http.HeaderAttribute;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;

public abstract class HttpServlet extends AbstractHttpServlet {

	public static final Logger log = WSE.getLogger();

	private static Method get(String name) {
		try {
			return HttpServlet.class.getMethod(name, HttpServletRequest.class, HttpServletResponse.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Method doGet = get("doGet");
	private static Method doPost = get("doPost");
	private static Method doHead = get("doHead");
	private static Method doOptions = get("doOptions");
	private static Method doPut = get("doPut");
	private static Method doPatch = get("doPatch");
	private static Method doDelete = get("doDelete");
	private static Method doSecure = get("doSecure");

	private final static Map<Method, HttpMethod> methods = new HashMap<>();
	static {
		methods.put(doGet, HttpMethod.GET);
		methods.put(doPost, HttpMethod.POST);
		methods.put(doHead, HttpMethod.HEAD);
		methods.put(doOptions, HttpMethod.OPTIONS);
		methods.put(doPut, HttpMethod.PUT);
		methods.put(doPatch, HttpMethod.PATCH);
		methods.put(doDelete, HttpMethod.DELETE);
		methods.put(doSecure, HttpMethod.SECURE);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doGet)) {
			notAllowed(response);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doPost)) {
			notAllowed(response);
		}
	}

	public void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doHead)) {
			notAllowed(response);
		}
	}

	public void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doOptions)) {

			getOptions(response.getHttpHeader());

			response.setStatusCode(HttpCodes.OK);
			String allow = ArrayUtils.join(this.getAllowedMethods(), ", ");
			response.setAttribute("Allow", allow);
			response.setAttribute("Access-Control-Allow-Origin", "*");
			response.setAttribute("Access-Control-Allow-Methods", allow);

			HeaderAttribute acrh = request.getAttribute("Access-Control-Request-Headers");
			if (acrh != null) {
				response.setAttribute("Access-Control-Allow-Headers", acrh.value);
			}

			response.setContentLength(0);
			response.writeHeader();
		}
	}

	/**
	 * A good servlet should override this method and fill the header parameter with
	 * info about this servlet. This is called by doOptions when the http verb
	 * OPTIONS is used in a request.
	 */
	public void getOptions(HttpHeader header) {
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doPut)) {
			notAllowed(response);
		}
	}

	public void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doPatch)) {
			notAllowed(response);
		}
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doDelete)) {
			notAllowed(response);
		}
	}

	public void doSecure(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!ClassUtils.isOverridden(this.getClass(), doSecure)) {
			notAllowed(response);
		}
	}

	private final void notAllowed(HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(getAllowedMethods());
	}

	private final HttpMethod[] getAllowedMethods() {
		Set<HttpMethod> supported = new HashSet<>();

		for (Entry<Method, HttpMethod> e : methods.entrySet()) {
			if (ClassUtils.isOverridden(this.getClass(), e.getKey())) {
				supported.add(e.getValue());
			}
		}

		supported.add(HttpMethod.OPTIONS);

		return supported.toArray(new HttpMethod[supported.size()]);
	}
}
