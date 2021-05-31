package wse.server.servlet.soap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;

import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.server.servlet.OperationListener;
import wse.utils.ClassUtils;
import wse.utils.ComplexType;
import wse.utils.HttpCodes;
import wse.utils.MimeType;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseBuildingException;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class OperationServlet extends SoapServlet {

	private final Map<String, Receiver> receiver = new HashMap<>();
	private OperationListener instance;

	public OperationServlet(OperationListener listener) {
		this.instance = listener;
		loadReceivers(listener.getClass());
	}

	public OperationServlet(final Class<? extends OperationListener> listener)
			throws InstantiationException, IllegalAccessException {
		this.instance = listener.newInstance();
		loadReceivers(listener);
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
		log.info("OperationServlet of " + this.instance.getClass().getSimpleName() + " listenening for " + receiver.size() + " operation" + (receiver.size() == 1 ? "" : "s") + (log.isLoggable(Level.FINE) ? ":" : ""));
		if (log.isLoggable(Level.FINE)) {
			for (Entry<String, Receiver> e : receiver.entrySet()) {
				log.fine("    '" + e.getKey() + "' - " + e.getValue().method.toGenericString());
			}
		}
	}

	@Override
	public void doSoap(HttpServletRequest request, XMLElement requestSoapHeader, XMLElement requestSoapBody, HttpServletResponse response)
			throws IOException {
		if (requestSoapBody == null) {
			response.sendError(400, "Envelope did not contain a Body element");
			return;
		}
		
		for (XMLElement hc : requestSoapHeader.getChildren()) {
			if (Objects.equals(hc.getAttributeValue("mustUnderstand"), "1")) {
				throw new SoapFault(SoapFault.MUST_UNDERSTAND, "Failed to understand header element \"" + hc.getName() + "\"");
			}
		}
		
		String soapAction = request.getAttributeValue("SOAPAction");
		
		
		// TODO check URI
		Receiver m = receiver.get(soapAction);
		if (m == null) {
			response.sendError(HttpCodes.NOT_FOUND, "Unknown SOAPAction: '" + soapAction + "'");
			return;
		}
		
		ComplexType result;
		try {
			result = m.invoke(requestSoapBody);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			response.sendError(HttpCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			e.printStackTrace();
			return;
		}
		
		XMLElement responseSoapEnvelope = XMLUtils.createSOAPFrame();
		XMLElement responseSoapBody = responseSoapEnvelope.getChild("Body");		
		
		try {	
			result.create(responseSoapBody);
		}catch(WseBuildingException e) {
			response.sendError(HttpCodes.INTERNAL_SERVER_ERROR, e);
			return;
		}
		
		XMLUtils.resetNamespaces(responseSoapEnvelope);
		
		byte[] responseContent = responseSoapEnvelope.toByteArray();
		
		response.setContentType(MimeType.application.xml, responseSoapEnvelope.getTree().getEncoding());
		response.setContentLength(responseContent.length);
		response.write(responseContent);
		
		return;
	}

	public String getRequestedPath() {
		return this.instance.path();
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

		public ComplexType invoke(XMLElement xml)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException {
			ComplexType i = input.newInstance();
			ComplexType o = output.newInstance();
			i.load(xml);

//			System.out.println(i.getClass());
//			System.out.println(o.getClass());
//			System.out.println(method.toGenericString());
			method.invoke(OperationServlet.this.instance, i, o);
			return o;
		}

		public String getSoapAction() {
			return soapAction;
		}

	}

}
