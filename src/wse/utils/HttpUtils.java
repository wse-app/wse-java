package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import wse.WSE;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseException;
import wse.utils.exception.WseHttpParsingException;
import wse.utils.exception.WseHttpStatusCodeException;
import wse.utils.exception.WseXMLBuildingException;
import wse.utils.exception.WseXMLParsingException;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public final class HttpUtils extends HttpCodes {
	private HttpUtils() {
	}

	private static final Logger log = WSE.getLogger();

	protected static final String AUTHORIZATION = "Authorization";
	protected static final String CONNECTION = "Connection";
	protected static final String SOAP_ACTION = "SOAPAction";
	protected static final String CONNECTION_CLOSE = "close";
	protected static final String CONTENT_TYPE = "Content-Type";

	public static void sendReceive(final HttpCall caller, final ComplexType requestMessage,
			final ComplexType responseMessage) {

		CallHandler call;

		call = new CallHandler(HttpMethod.POST, caller.getTarget(), new HttpWriter() {

			byte[] data = null;
			Charset charset;

			private void getData() {
				if (data != null)
					return;
				XMLElement soapXML = XMLUtils.createSOAPFrame();
				XMLElement body = soapXML.getChild("Body");

				if (requestMessage != null) {
					try {
						requestMessage.create(body);
					} catch (Exception e) {
						throw new WseXMLBuildingException("Failed to create request XML: " + e.getMessage(), e);
					}
				}

				XMLUtils.resetNamespaces(soapXML);
				data = soapXML.toByteArray();
				charset = soapXML.getTree().getCharset();
			}

			@Override
			public void prepareHeader(HttpHeader header) {
				header.setAttribute(SOAP_ACTION, caller.getSoapAction());
				header.setContentType(MimeType.text.xml.withCharset(charset));
			}

			@Override
			public void writeToStream(OutputStream output, Charset charset) throws IOException {
				getData();
				if (data == null)
					return;
				output.write(data);
			}

			@Override
			public long requestContentLength() {
				getData();
				return data != null ? data.length : 0;
			}
		}, caller.getSSLStore());

		log.info("Calling service: " + caller);
		try {

			HttpResult result;
			try {
				result = call.call();

			} catch (WseException e) {
				// Content may contain fault

				if (!(e.getCause() instanceof WseHttpStatusCodeException)) // Thrown by a bad status code
					throw e;

				HttpResult http = call.responseHttp;
				if (http == null || http.getHeader() == null || http.getContent() == null
						|| !http.getHeader().getContentType().is(MimeType.text.xml))
					throw e;
				XMLElement xml = XMLUtils.parseSimple(http.getContent());
				if (xml != null)
					xml = xml.getChild("Body", xml.getNamespaceURI());
				if (xml != null)
					xml = xml.getChild("Fault", xml.getNamespaceURI());
				if (xml == null)
					throw e;
				SoapFault sf = new SoapFault(xml);
				throw sf;
			}

			if (result == null)
				throw new WseHttpParsingException("Got null result");

			XMLElement responseFile;

			try {
				InputStream content = result.getContent();
				responseFile = XMLUtils.parseSimple(content);
				content.close();
			} catch (Exception e) {
				throw new WseXMLParsingException("Failed to parse xml: " + e.getMessage(), e);
			} finally {
				try {
					result.getContent().close();
				} catch (Exception e) {
				}
			}

			if (responseFile == null) {
				log.severe("Failed to parse xml");
				throw new WseXMLParsingException("Failed to parse xml");
			}
			XMLElement soap_body = responseFile.getChild("Body", XMLUtils.SOAP_ENVELOPE);
			if (soap_body == null) {
				log.severe("Soap body is missing");
				throw new WseXMLParsingException("Soap body is missing");
			}

			if (responseMessage != null) {
				try {
					responseMessage.load(soap_body);
				} catch (Exception e) {
					throw new WseXMLParsingException("Failed to load response XML: " + e.getMessage(), e);
				}
			}

			log.info("Call successful");
		} catch (Throwable e) {
			if (e.getMessage() != null)
				log.severe(e.getMessage());

			if (e instanceof SoapFault)
				throw (SoapFault) e;

			throw new WseException("Call failed: " + e.getMessage(), e);
		}
	}

}
