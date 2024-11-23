package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends WseInputStream {

	private long limit;
	private long counter;

	public LimitedInputStream(long limit) {
		this(null, limit);
	}

	public LimitedInputStream(InputStream readFrom, long limit) {
		super(readFrom);
		this.limit = limit;
	}

	@Override
	public int read() throws IOException {
		if (counter >= limit)
			return -1;

		int b = super.read();
		if (b != -1) {
			counter++;
		}
		return b;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		long left = limit - counter;
		if (left == 0) {
			return -1;
		}
		int max = (int) Math.min(len, left);
		int actual = super.read(b, off, max);
		if (actual != -1)
			counter += actual;
		return actual;
	}

	@Override
	public int available() throws IOException {
		long left = limit - counter;
		if (left <= 0) {
			return 0;
		}
		return (int) Math.min(left, readFrom.available());
	}

	@Override
	public String infoName() {
		return super.infoName() + " (" + this.counter + "/" + this.limit + ")";
	}

}
