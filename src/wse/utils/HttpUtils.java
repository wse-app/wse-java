package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import wse.WSE;
import wse.client.IOConnection;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseConnectionException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseHttpParsingException;
import wse.utils.exception.WseHttpStatusCodeException;
import wse.utils.exception.WseXMLParsingException;
import wse.utils.http.HttpBuilder;
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

		HttpWriter writer = new SoapXMLWriter(caller.getSoapAction(), requestMessage);
		CallHandler call = new CallHandler(HttpMethod.POST, caller.getTarget(), writer, caller.getSSLStore());
		call.setOptions(caller);

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

			if (e instanceof WseException)
				throw (WseException) e;

			throw new WseException("Call failed: " + e.getMessage(), e);
		}
	}

	public static HttpResult read(IOConnection connection, boolean modifyContent) {
		InputStream input;
		try {
			input = connection.getInputStream();
		} catch (IOException e) {
			throw new WseConnectionException(e);
		}
		return read(input, modifyContent);
	}

	public static HttpResult read(InputStream inputStream, boolean modifyContent) {
		try {
			return HttpBuilder.read(inputStream, modifyContent);
		} catch (IOException e) {
			throw new WseConnectionException("Failed to read: " + e.getMessage(), e);
		}
	}

}
