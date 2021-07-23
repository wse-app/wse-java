package wse.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public abstract class HttpHeaderWriter extends HttpWriter {
	@Override
	public final long requestContentLength() { return 0; }
	
	@Override
	public final void writeToStream(OutputStream stream, Charset charset) throws IOException {}
}
