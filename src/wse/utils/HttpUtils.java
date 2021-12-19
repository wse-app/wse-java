package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Logger;

import wse.WSE;
import wse.client.IOConnection;
import wse.utils.exception.SoapFault;
import wse.utils.exception.WseConnectionException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseHttpParsingException;
import wse.utils.exception.WseHttpStatusCodeException;
import wse.utils.exception.XMLException;
import wse.utils.http.HttpBuilder;
import wse.utils.http.HttpMethod;
import wse.utils.writer.HttpWriter;
import wse.utils.writer.SoapXMLWriter;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public final class HttpUtils extends HttpCodes {
	private HttpUtils() {
	}

	private static final Logger log = WSE.getLogger();

	public static final String AUTHORIZATION = "Authorization";
	public static final String SOAP_ACTION = "SOAPAction";

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
				throw new SoapFault(xml);
			}

			log.info("HttpUtils.sendReceive() XML PARSING...");

			if (result == null)
				throw new WseHttpParsingException("Got null result");

			XMLElement responseFile;
			InputStream content = result.getContent();
			try {
				responseFile = XMLUtils.parseSimple(content);
				log.info("HttpUtils.sendReceive() XML PARSING DONE");
			} catch (Throwable e) {
				log.info("HttpUtils.sendReceive() XML PARSING FAILED");
				throw new XMLException("Failed to parse xml: " + e.getMessage(), e);
			}

			if (responseFile == null) {
				log.severe("Failed to parse xml");
				throw new XMLException("Failed to parse xml");
			}
			XMLElement soap_body = responseFile.getChild("Body", XMLUtils.SOAP_ENVELOPE);
			if (soap_body == null) {
				log.severe("Soap body is missing");
				throw new XMLException("Soap body is missing");
			}

			if (responseMessage != null) {
				try {
					responseMessage.load(soap_body);
				} catch (Exception e) {
					throw new XMLException("Failed to load response XML: " + e.getMessage(), e);
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

	/** Never returns null */
	public static HttpResult read(IOConnection connection, boolean modifyContent) throws IOException {
		Objects.requireNonNull(connection, "connection == null");

		InputStream input = connection.getInputStream();
		Objects.requireNonNull(input, "connection inputstream == null");

		return read(input, modifyContent);
	}

	/** Never returns null */
	public static HttpResult read(InputStream inputStream, boolean modifyContent) {
		try {
			return HttpBuilder.read(inputStream, modifyContent);
		} catch (IOException e) {
			throw new WseConnectionException("Failed to read: " + e.getMessage(), e);
		}
	}

}
