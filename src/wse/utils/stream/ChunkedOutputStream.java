package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public class ChunkedOutputStream extends BufferedOutputStream {

	private static final byte[] end = "0\r\n\r\n".getBytes();

	/*
	 * https://tools.ietf.org/html/rfc2616#page-25
	 * 
	 * [...] chunk-size [chunk-extension] "\r\n" chunk_data "\r\n"
	 * 
	 * chunk-extension = ";" chunk-ext-name ["=" chunk-ext-val]
	 */

	public void setWriteTo(OutputStream writeTo) {
		this.writeTo = writeTo;
	}

	public ChunkedOutputStream(int chunkSize) {
		this(null, chunkSize);
	}

	public ChunkedOutputStream(OutputStream writeTo, int chunkSize) {
		super(writeTo, chunkSize, 256 + 2, 2);
		super.setPermanentSuffix("\r\n".getBytes());
		super.setPermanentPrefix("\r\n".getBytes());
	}

	@Override
	protected void writeBuffer(byte[] data, int content_offset, int prefix_length, int content_length,
			int suffix_length) throws IOException {
		if (content_length == 0) {
			return;
		}

		String len = Integer.toHexString(content_length);
		byte[] lenData = len.getBytes();

		System.arraycopy(lenData, 0, data, content_offset - prefix_length - lenData.length, lenData.length);

		super.writeBuffer(data, content_offset, prefix_length + lenData.length, content_length, suffix_length);
	}

	private void sendEnd() throws IOException {
		writeTo.write(end);
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		sendEnd();
		writeTo.flush();
	}
}
