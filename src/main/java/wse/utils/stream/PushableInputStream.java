package wse.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PushableInputStream extends WseInputStream implements IsPushableInputStream {

	private ArrayList<InputStream> first = new ArrayList<>(10);

	public PushableInputStream(InputStream readFrom) {
		super(readFrom);
	}

	public void push(byte[] data, int offset, int len) {
		byte[] b = new byte[len];
		System.arraycopy(data, offset, b, 0, len);
		synchronized (first) {
			first.add(0, new ByteArrayInputStream(b));
		}
	}

	@Override
	public int available() throws IOException {

		long res = 0;
		synchronized (first) {
			for (InputStream is : first) {
				res += is.available();
			}
		}

		res += super.available();
		return (int) Math.min(res, Integer.MAX_VALUE);
	}

	@Override
	public long skip(long n) throws IOException {
		long skipped = 0;
		long a = 0;
		InputStream is;
		synchronized (first) {
			for (int i = 0; i < first.size(); i++) {
				is = first.get(i);
				a = is.skip(n - skipped);
				if (a < n - skipped) {
					first.remove(i);
					i--;
				}
				skipped += a;
			}
		}
		skipped += super.skip(n - skipped);
		return skipped;
	}

	@Override
	public int read() throws IOException {
		InputStream is;
		synchronized (first) {
			for (int i = 0; i < first.size(); i++) {
				is = first.get(i);
				int read = is.read();
				if (read == -1) {
					is.close();
					first.remove(i);
					i--;
					continue;
				}
				return read;
			}
		}
		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		InputStream is;
		synchronized (first) {
			for (int i = 0; i < first.size(); i++) {
				is = first.get(i);
				int read = is.read(b, off, len);
				if (read == -1) {
					is.close();
					first.remove(i);
					i--;
					continue;
				}
				return read;
			}
		}

		return super.read(b, off, len);
	}

}
