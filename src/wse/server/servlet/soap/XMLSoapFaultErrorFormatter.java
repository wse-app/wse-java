package wse.server.servlet.soap;

import wse.utils.MimeType;
import wse.utils.exception.SoapFault;
import wse.utils.http.HttpAttributeList;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class XMLSoapFaultErrorFormatter extends SoapFaultErrorFormatter {
	
	public byte[] error(SoapFault fault, HttpAttributeList attributes) {
		XMLElement xml = new XMLElement("Fault");
		fault.create(xml);
		XMLElement env = XMLUtils.createSOAPFrame();
		XMLElement body = env.getChild("Body");
		
		attributes.setContentType(MimeType.text.xml);
		
		body.addChild(xml);
		XMLUtils.resetNamespaces(env);
		
		return env.toByteArray();
	}
	
}
