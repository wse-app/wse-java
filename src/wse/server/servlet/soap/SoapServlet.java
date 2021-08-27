package wse.server.servlet.soap;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;

import wse.server.servlet.HttpServlet;
import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.utils.MimeType;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseParsingException;
import wse.utils.exception.XMLException;
import wse.utils.http.ContentType;
import wse.utils.http.HttpMethod;
import wse.utils.internal.IElement;
import wse.utils.internal.ILeaf;
import wse.utils.internal.InternalFormat;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public abstract class SoapServlet extends HttpServlet {

	@Override
	public final void doAny(HttpServletRequest request, HttpServletResponse response) throws IOException {
		CORSAllowAll(response);
		super.doAny(request, response);
	}

	public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(HttpMethod.POST);
	}

	public final void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(HttpMethod.POST);
	}

	public final void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentLength(0);
		response.writeHeader();
	}

	public final void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(HttpMethod.POST);
	}

	public final void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(HttpMethod.POST);
	}

	public final void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(HttpMethod.POST);
	}

	public final void doSecure(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendMethodNotAllowed(HttpMethod.POST);
	}

	@Override
	public final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ContentType ct = request.getContentType();

		MimeType mt = null;
		Charset cs = null;

		if (ct != null) {
			mt = MimeType.getByName(ct.getMimeType());
			cs = ct.getCharsetParsed();
		}

		if (mt == null)
			mt = MimeType.application.xml;

		if (cs == null)
			cs = Charset.forName("UTF-8");

		if (mt == MimeType.application.xml || mt == MimeType.text.xml) {
			response.setErrorFormatter(new XMLSoapFaultErrorFormatter());
		} else if (mt == MimeType.application.json) {
			response.setErrorFormatter(new JSONSoapFaultErrorFormatter());
		}

		IElement ie;
		try {
			ILeaf il = InternalFormat.parse(mt, request.getContent(), cs);

			if (!(il instanceof IElement)) {
				// TODO Support ILeaf structure for very simple requests?
				throw new WseParsingException("Only tree-based content types are supported.");
			}

			ie = (IElement) il;

		} catch (Exception e) {
			response.sendError(400, e);
			log.log(Level.FINEST, e.getClass().getName() + ": " + e.getMessage(), e);
			return;
		}

		if (ie instanceof XMLElement) {
			try {
				validateSoap((XMLElement) ie);
			} catch (Exception e) {
				response.sendError(400, e);
				return;
			}
		}

		ie = soapUnwrap(ie);

		try {
			doSoap(request, mt, ie, response);
		} catch (Throwable e) {
			response.sendError(500, e);
			return;
		}
	}

	public IElement soapUnwrap(IElement ie) {

		if (!(ie instanceof XMLElement))
			return ie;

		// Unwrap XML SOAP
		XMLElement envelope = (XMLElement) ie;
		XMLElement body = envelope.getChild("Body", XMLUtils.SOAP_ENVELOPE);
		if (body == null)
			throw new SoapFault(SoapFault.CLIENT, "Missing soap envelope Body element");

		return body;
	}

	public IElement soapWrap(IElement ie) {

		if (!(ie instanceof XMLElement))
			return ie;

		XMLElement soap = XMLUtils.createSOAPFrame(false);

		soap.setChild("Body", XMLUtils.SOAP_ENVELOPE, ie);

		XMLUtils.resetNamespaces(soap);

		return soap;

	}

	public abstract void doSoap(HttpServletRequest request, MimeType contentType, IElement content,
			HttpServletResponse response) throws IOException;

	public void validateSoap(XMLElement envelope) {
		if (!Objects.equals(envelope.getName(), "Envelope")
				|| !Objects.equals(envelope.getNamespaceURI(), XMLUtils.SOAP_ENVELOPE))
			throw new XMLException("XML Content is not SOAP");

		XMLElement header = envelope.getChild("Header", XMLUtils.SOAP_ENVELOPE);

		for (XMLElement hc : header.getChildren()) {
			if (Objects.equals(hc.getAttributeValue("mustUnderstand"), "1")) {
				if (!canUnderstand(hc))
					throw new SoapFault(SoapFault.MUST_UNDERSTAND,
							"Failed to understand header element \"" + hc.getName() + "\"");
			}
		}

	}

	public abstract boolean canUnderstand(XMLElement xml);

	public abstract boolean supportsSoapAction(String soapAction);

}
