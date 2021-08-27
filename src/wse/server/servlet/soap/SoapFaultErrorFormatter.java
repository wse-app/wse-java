package wse.server.servlet.soap;

import wse.utils.exception.SoapFault;
import wse.utils.http.ErrorFormatter;
import wse.utils.http.HttpAttributeList;

public abstract class SoapFaultErrorFormatter implements ErrorFormatter {

	@Override
	public byte[] error(int code, String message, HttpAttributeList attributes) {
		return error(new SoapFault(code, message), attributes);
	}

	@Override
	public byte[] error(int code, Throwable cause, HttpAttributeList attributes) {
		return error(cause instanceof SoapFault ? (SoapFault) cause : new SoapFault(code, cause), attributes);
	}

	public abstract byte[] error(SoapFault fault, HttpAttributeList attributes);

}
