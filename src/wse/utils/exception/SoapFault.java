package wse.utils.exception;

import java.util.Objects;

import wse.utils.HttpCodes;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class SoapFault extends WseHttpException {
	private static final long serialVersionUID = -6803733893368261338L;
	public static final String CLIENT = "SOAP-ENV:Client";
	public static final String SERVER = "SOAP-ENV:Server";
	public static final String VERSION_MISMATCH = "SOAP-ENV:VersionMismatch";
	public static final String MUST_UNDERSTAND = "SOAP-ENV:MustUnderstand";
	
	public static String httpCodeText(int httpCode) {
		if (httpCode >= 400 && httpCode < 500)
			return CLIENT;
		if (httpCode >= 500)
			return SERVER;
		return null;
	}
	
	private String faultcode;
	private String faultactor;
	private XMLElement detail;
	private XMLElement xmlSource;

	public SoapFault(XMLElement fault) {
		super(fault == null ? null : fault.getChildValue("faultstring"));
		try {
			this.xmlSource = fault;
			if (fault != null) {
				this.faultactor = fault.getChildValue("faultactor");
				this.faultcode = fault.getChildValue("faultcode");
				this.detail = fault.getChild("detail");
				if (Objects.equals(this.faultcode, SERVER)) {
					setStatusCode(HttpCodes.INTERNAL_SERVER_ERROR);
				}else {
					setStatusCode(400);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public SoapFault(Throwable cause) {
		this(cause.getClass().getSimpleName(), cause.getMessage());
	}
	
	public SoapFault(int code, Throwable cause) {
		this(code, cause.getMessage());
	}
	
	public SoapFault(String message) {
		this(SERVER, message);
	}
	
	
	public SoapFault(int code, String message) {
		this(httpCodeText(code), message);
	}
	public SoapFault(String code, String message) {
		this(code, message, "WSE", null);
	}
	
	public SoapFault(int code, String message, String actor, XMLElement detail) {
		this(httpCodeText(code), message, actor, detail);
	}
	public SoapFault(String code, String message, String actor, XMLElement detail) {
		super(message);
		this.faultcode = code;
		this.faultactor = actor;
		this.detail = detail;
	}

	public String getFaultCode() {
		return faultcode;
	}

	public String getFaultString() {
		return getMessage();
	}

	public String getFaultActor() {
		return faultactor;
	}

	public XMLElement getDetail() {
		return detail;
	}

	public void setFaultCode(String faultcode) {
		this.faultcode = faultcode;
	}

	public void setFaultActor(String faultactor) {
		this.faultactor = faultactor;
	}

	public void setDetail(XMLElement detail) {
		this.detail = detail;
	}

	public XMLElement getXMLSource() {
		return xmlSource;
	}

	public XMLElement toXML() {
		XMLElement xml = new XMLElement("Fault", XMLUtils.SOAP_ENVELOPE);

		String msg = this.getMessage();
		if (msg != null)
			xml.addChildValue("faultstring", null, msg);
		if (faultcode != null)
			xml.addChildValue("faultcode", null, faultcode);
		if (faultactor != null)
			xml.addChildValue("faultactor", null, faultactor);
		if (detail != null)
			xml.addChild(detail);

		return xml;
	}
}