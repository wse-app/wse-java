package wse.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import wse.utils.exception.WseXMLBuildingException;
import wse.utils.http.HttpHeader;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class SoapXMLWriter extends HttpWriter {

	private byte[] data = null;
	private Charset charset;

	private final ComplexType requestType;
	private final String soapAction;
	
	public SoapXMLWriter(String soapAction, ComplexType requestType) {
		this.soapAction = soapAction;
		this.requestType = requestType;
	}
	
	private void getData() {
		if (data != null)
			return;
		XMLElement soapXML = XMLUtils.createSOAPFrame();
		XMLElement body = soapXML.getChild("Body");

		if (requestType != null) {
			try {
				requestType.create(body);
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
		header.setAttribute(HttpUtils.SOAP_ACTION, this.soapAction);
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

}