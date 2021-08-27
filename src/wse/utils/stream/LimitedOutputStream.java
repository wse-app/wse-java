package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public class LimitedOutputStream extends WseOutputStream {

	private long limit;
	private long counter;

	public LimitedOutputStream(long limit) {
		this(null, limit);
	}

	public LimitedOutputStream(OutputStream writeTo, long limit) {
		super(writeTo);
		this.limit = limit;
	}

	@Override
	public void write(int b) throws IOException {
		if (counter >= limit)
			return;
		writeTo.write(b);
		counter++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		long left = limit - counter;
		if (left == 0)
			return;
		if (left >= len) {
			writeTo.write(b, off, len);
			counter += len;
			return;
		}
		super.write(b, off, (int) left);
		counter += left;
	}

}
