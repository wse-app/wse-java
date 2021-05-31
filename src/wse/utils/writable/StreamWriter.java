package wse.utils.writable;

import java.io.IOException;
import java.io.OutputStream;

public interface StreamWriter
{
	/**
	 * Writes the object to a stream
	 * @param stream the stream to be written on
	 * @throws IOException 
	 */
	public void writeToStream(OutputStream stream) throws IOException;
	
}
