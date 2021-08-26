package wse.utils.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import wse.utils.MimeType;
import wse.utils.http.HttpHeader;

public class ByteArrayWriter implements HttpWriter {

	private final byte[] data;
	private final MimeType mt;
	private final Charset cs;

	public ByteArrayWriter(byte[] data, MimeType mt, Charset cs) {
		this.data = data != null ? data : new byte[0];
		this.mt = mt != null ? mt : MimeType.application.octet_stream;
		this.cs = cs;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		if (data.length > 0)
			stream.write(data);
	}

	@Override
	public void prepareHeader(HttpHeader header) {
		if (data.length > 0)
			header.setContentType(mt.withCharset(cs));
	}

	@Override
	public long requestContentLength(Charset cs) {
		return data.length;
	}

}
