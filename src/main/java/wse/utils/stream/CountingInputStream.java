package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;

public class CountingInputStream extends WseInputStream {

	private String name;
	private long count;
	private boolean done = false;
	private final Logger log;

	public CountingInputStream(InputStream readFrom) {
		this(readFrom, readFrom.getClass().getName());
	}

	public CountingInputStream(InputStream readFrom, String name) {
		this(readFrom, name, WSE.getLogger());
	}
	
	public CountingInputStream(InputStream readFrom, Logger log) {
		this(readFrom, readFrom.getClass().getName(), log);
	}
	
	public CountingInputStream(InputStream readFrom, String name, Logger log) {
		super(readFrom);
		this.name = name;
		this.log = log;
	}

	@Override
	public int read() throws IOException {
		int a = super.read();
		log.log(Level.FINEST, name + " read() " + (a != -1 ? 1 : -1));
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
		log.log(Level.FINEST, name + " read() " + a);
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
