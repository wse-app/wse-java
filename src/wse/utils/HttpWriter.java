package wse.utils;

import wse.utils.http.HttpHeader;
import wse.utils.writable.StreamWriter;

public abstract class HttpWriter implements StreamWriter {
	
	/** This method is to be overriden if the caller wants to add additional attributes such as Content-Type or SOAPAction<br>
	 * <br>
	 * Note: Content-Length is set automatically set to the returned value from {@link HttpWriter#requestContentLength()} if >= 0 
	 * */
	public abstract void prepareHeader(HttpHeader header);
	
	/** This method is called before header is written, if returned value >= 0, then header attribute Content-Length is set to that */
	public abstract long requestContentLength();
}
