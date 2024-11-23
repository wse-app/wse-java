package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class PushableBufferedInputStream extends BufferedInputStream implements IsPushableInputStream {

	private byte[] push;
	private int push_counter;
	private int push_offset;
	private int push_length;

	public PushableBufferedInputStream(InputStream readFrom, int bufferSize) {
		super(readFrom, bufferSize);
	}

	@Override
	public int read() throws IOException {
		if (push != null && push_length - push_counter != 0) {
			push_counter++;
			return push[push_offset + (push_counter - 1)];
		} else {
			this.push = null;
		}

		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (push != null && push_length - push_counter != 0) {
			int av = push_length - push_counter;
			if (av >= len) {
				System.arraycopy(push, push_offset + push_counter, b, off, len);
				push_counter += len;
				push_offset += len;
				return len;
			} else {
				System.arraycopy(push, push_offset + push_counter, b, off, av);
				push = null;
				return av;
			}
		} else {
			this.push = null;
		}

		return super.read(b, off, len);
	}

	public void push(byte[] data, int off, int len) {
		push = data;
		push_length = len;
		push_offset = off;
		push_counter = 0;
	}

}
