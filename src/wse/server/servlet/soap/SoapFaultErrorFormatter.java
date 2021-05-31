package wse.server.servlet.soap;

import wse.utils.MimeType;
import wse.utils.exception.SoapFault;
import wse.utils.http.ErrorFormatter;
import wse.utils.http.HttpAttributeList;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class SoapFaultErrorFormatter implements ErrorFormatter {

	@Override
	public byte[] error(int code, String message, HttpAttributeList attributes) {
		return error(new SoapFault(code, message), attributes);
	}

	@Override
	public byte[] error(int code, Throwable cause, HttpAttributeList attributes) {
		return error(cause instanceof SoapFault ? (SoapFault) cause : new SoapFault(code, cause), attributes);
	}
	
	public byte[] error(SoapFault fault, HttpAttributeList attributes) {
		XMLElement xml = fault.toXML();
		XMLElement env = XMLUtils.createSOAPFrame();
		XMLElement body = env.getChild("Body");
		
		attributes.setContentType(MimeType.text.xml);
		
		body.addChild(xml);
		
		XMLUtils.resetNamespaces(env);
		return env.toByteArray();
	}
	
}
