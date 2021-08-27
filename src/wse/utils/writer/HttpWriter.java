package wse.utils.writer;

import java.nio.charset.Charset;

import wse.utils.HttpHeaderWriter;
import wse.utils.writable.StreamWriter;

public interface HttpWriter extends StreamWriter, HttpHeaderWriter {

	/**
	 * This method is called before header is written, if returned value >= 0, then
	 * header attribute Content-Length is set to that
	 */
	public abstract long requestContentLength(Charset cs);
}
