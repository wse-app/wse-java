package wse.server.servlet.soap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.utils.ClassUtils;
import wse.utils.ComplexType;
import wse.utils.HttpCodes;
import wse.utils.MimeType;
import wse.utils.exception.WseBuildingException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseParsingException;
import wse.utils.internal.IElement;
import wse.utils.writable.StreamCatcher;
import wse.utils.xml.XMLElement;

public class OperationServlet extends SoapServlet {

	private static final Logger LOG = WSE.getLogger();

	private final Map<String, Receiver> receiver = new HashMap<>();
	private final OperationListener instance;
	private final Class<? extends OperationListener> listenerClass;

	public OperationServlet(OperationListener listener) {
		this.instance = listener;
		this.listenerClass = listener.getClass();
		loadReceivers(listener.getClass());
	}

	public OperationServlet(final Class<? extends OperationListener> listener)
			throws InstantiationException, IllegalAccessException {
		this.listenerClass = listener;
		this.instance = null;
		loadReceivers(listener);
	}

	public OperationListener getInstance() {
		if (instance != null)
			return instance;
		try {
			return listenerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Failed to instantiate OperationListener", e);
			throw new WseException(e.getMessage(), e);
		}
	}

	private void loadReceivers(final Class<? extends OperationListener> listener) {
		Method[] ms = listener.getMethods();
		Class<?>[] params;
		for (Method m : ms) {

			params = m.getParameterTypes();
			if (params == null || params.length != 2)
				continue;
			if (ComplexType.class.isAssignableFrom(params[0]) && ComplexType.class.isAssignableFrom(params[1])) {

				OperationHandler h = ClassUtils.getAnnotation(m, OperationHandler.class);
				if (h == null || h.value() == null)
					continue;
				receiver.put(h.value(), new Receiver(m, params, h));
			}
		}
		LOG.info("OperationServlet of " + this.listenerClass.getSimpleName() + " listenening for " + receiver.size()
				+ " operation" + (receiver.size() == 1 ? "" : "s") + (LOG.isLoggable(Level.FINE) ? ":" : ""));
		if (LOG.isLoggable(Level.FINE)) {
			for (Entry<String, Receiver> e : receiver.entrySet()) {
				LOG.fine("    '" + e.getKey() + "' - " + e.getValue().method.toGenericString());
			}
		}
	}

	@Override
	public void doSoap(HttpServletRequest request, MimeType contentType, IElement content, HttpServletResponse response)
			throws IOException {

		String soapAction = request.getAttributeValue("SOAPAction");

		// TODO check URI, ???
		Receiver m = receiver.get(soapAction);
		if (m == null) {
			response.sendError(HttpCodes.NOT_FOUND, "Unknown SOAPAction: '" + soapAction + "'");
			return;
		}

		OperationListener instance = getInstance();
		instance.request = request;
		instance.response = response;

		ComplexType result;
		try {
			result = m.invoke(instance, content);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			response.sendError(HttpCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			LOG.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
			return;
		} catch (WseParsingException e) {
			response.sendError(400, e);
			return;
		}

		IElement ie = content.createEmpty();

		try {
			result.create(ie);
		} catch (WseBuildingException e) {
			response.sendError(HttpCodes.INTERNAL_SERVER_ERROR, e);
			return;
		}

		ie = soapWrap(ie);

		StreamCatcher catcher = new StreamCatcher();

		Charset charset = ie.preferredCharset();
		if (charset == null)
			charset = Charset.forName("UTF-8");

		ie.writeToStream(catcher, charset);

		byte[] responseContent = catcher.toByteArray();

		response.setContentType(contentType, charset);
		response.setContentLength(responseContent.length);
		response.write(responseContent);

		return;
	}

	public String getRequestedPath() {
		return getInstance().path();
	}

	public boolean supportsSoapAction(String soapAction) {
		return this.receiver.containsKey(soapAction);
	}

	protected class Receiver {
		private final String soapAction;
		private final Method method;
		private final Class<? extends ComplexType> input, output;

		public Receiver(Method m, Class<?>[] params, OperationHandler handler) {
			this.method = m;
			this.input = params[0].asSubclass(ComplexType.class);
			this.output = params[1].asSubclass(ComplexType.class);
			this.soapAction = handler.value();
		}

		public ComplexType invoke(OperationListener instance, IElement e) throws InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			ComplexType i = input.newInstance();
			ComplexType o = output.newInstance();
			i.load(e);

			method.invoke(instance, i, o);
			return o;
		}

		public String getSoapAction() {
			return soapAction;
		}

	}

	@Override
	public boolean canUnderstand(XMLElement xml) {
		return false;
	}

}
