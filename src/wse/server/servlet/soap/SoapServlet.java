package wse.server.servlet.soap;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import wse.server.servlet.HttpServlet;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.utils.http.HttpMethod;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public abstract class SoapServlet extends HttpServlet {
	
	@Override
	public final void doAny(HttpServletRequest request, HttpServletResponse response) throws IOException { super.doAny(request, response); }
	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	public final void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	public final void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	public final void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	public final void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	public final void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	public final void doSecure(HttpServletRequest request, HttpServletResponse response) throws IOException { response.sendMethodNotAllowed(HttpMethod.POST); }
	
	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setErrorFormatter(new SoapFaultErrorFormatter());
		XMLElement envelope;
		try {
			envelope = XMLUtils.parseSimple(request.getContent());
		} catch (ParserConfigurationException | SAXException | TransformerException e) {
			response.sendError(400, e);
			log.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage(), e);
			return;
		}
		
		if (!Objects.equals(envelope.getName(), "Envelope") || !Objects.equals(envelope.getNamespaceURI(), XMLUtils.SOAP_ENVELOPE)) {
			response.sendError(400, "Content is not SOAP");
			return;
		}
		
		XMLElement header = envelope.getChild("Header", XMLUtils.SOAP_ENVELOPE);
		XMLElement body = envelope.getChild("Body", XMLUtils.SOAP_ENVELOPE);
		
		try {
			doSoap(request, header, body, response);						
		}catch(Throwable e) {
			response.sendError(500, e.getMessage());
			return;
		}
	}
	
	public abstract void doSoap(HttpServletRequest request, XMLElement requestSoapHeader, XMLElement requestSoapBody, HttpServletResponse response) throws IOException;
	public abstract boolean supportsSoapAction(String soapAction);
	
}
