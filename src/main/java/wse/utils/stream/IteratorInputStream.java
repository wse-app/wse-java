package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class IteratorInputStream extends WseInputStream {

	private Iterator<InputStream> streams;
	private InputStream current;

	public IteratorInputStream(Iterator<InputStream> inputs) {
		super(null);
		getNext();
	}

	@Override
	public void setTarget(InputStream target) {
		throw new UnsupportedOperationException("IteratorInputStream can't change target");
	}

	private boolean getNext() {
		if (!streams.hasNext())
			return false;
		current = streams.next();
		return true;
	}

	@Override
	public int read() throws IOException {
		if (current == null)
			return -1;

		int read = current.read();
		if (read == -1) {
			if (!getNext())
				return -1;
			return read();
		}

		return read;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (current == null)
			return -1;

		int read = current.read(b, off, len);
		if (read == -1) {
			if (!getNext())
				return -1;
			return read();
		}

		return read;
	}

	@Override
	public int available() throws IOException {
		if (current != null)
			return current.available();
		return 0;
	}

}
