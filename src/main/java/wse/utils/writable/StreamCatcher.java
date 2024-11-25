package wse.utils.writable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import wse.utils.http.StreamUtils;
import wse.utils.stream.BufferedOutputStream;
import wse.utils.stream.CombinedInputStream;

public class StreamCatcher extends BufferedOutputStream implements StreamWriter {

	List<InputStream> streams = new LinkedList<>();

	public StreamCatcher() {
		super(null, 4096);
	}

	@Override
	protected void writeBuffer(byte[] data, int offset, int length) throws IOException {
		if (length > 0) {
			byte[] copy = new byte[length];
			System.arraycopy(data, offset, copy, 0, length);
			streams.add(new ByteArrayInputStream(copy));
		}
	}

	public int getSize() {
		return (int) this.total_write;
	}

	public void reset() {
		flush();
		this.total_write = 0;
		streams.clear();
	}

	public InputStream asInputStream() {
		flush();
		return new CombinedInputStream(streams);
	}

	@Override
	public void flush() {
		try {
			super.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] toByteArray() {
		byte[] result = new byte[(int) getSize()];
		try {
			StreamUtils.write(asInputStream(), result);
		} catch (IOException e) {
		}
		return result;
	}

	public static StreamCatcher from(StreamWriter writer, Charset charset) {
		StreamCatcher result = new StreamCatcher();
		try {
			if (writer != null)
				writer.writeToStream(result, charset);
		} catch (IOException ignore) {
		}
		return result;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		flush();
		StreamUtils.write(this.asInputStream(), stream, this.getSize());
	}
}
