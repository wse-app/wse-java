package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;

public class CountingInputStream extends WseInputStream {

	private static final Logger LOG = WSE.getLogger();
	private String name;
	private long count;
	private boolean done = false;

	public CountingInputStream(InputStream readFrom) {
		this(readFrom, readFrom.getClass().getName());
	}

	public CountingInputStream(InputStream readFrom, String name) {
		super(readFrom);
		this.name = name;
	}

	@Override
	public int read() throws IOException {
		int a = super.read();
		LOG.log(Level.FINEST, name + " read() " + (a != -1 ? 1 : -1));
		if (a != -1) {
			count += 1;
		} else {
			done = true;
		}
		return a;
	}

	public String getName() {
		return name;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int a = super.read(b, off, len);
		LOG.log(Level.FINEST, name + " read() " + a);
		if (a != -1) {
			count += a;
		} else {
			done = true;
		}
		return a;
	}

	@Override
	public String infoName() {
		return super.infoName() + " " + (done ? ("[" + count + "]") : count);
	}
}
