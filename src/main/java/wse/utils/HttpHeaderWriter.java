package wse.utils;

import wse.utils.http.HttpHeader;

public interface HttpHeaderWriter {
	/**
	 * This method is to be overriden if the caller wants to add additional
	 * attributes such as Content-Type or SOAPAction
	 */
	public void prepareHeader(HttpHeader header);
}
