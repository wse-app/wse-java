package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public abstract class WseInputStream extends InputStream implements IsInputStream {

	protected InputStream readFrom;

	public WseInputStream(InputStream readFrom) {
		super();
		this.readFrom = readFrom;
	}

	public void setTarget(InputStream target) {
		this.readFrom = target;
	}

	public InputStream getSource() {
		return this.readFrom;
	}

	@Override
	public int read() throws IOException {
		return readFrom.read();
	}

	@Override
	public final int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
//		if (true) return super.read(b, off, len);
		return readFrom.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		readFrom.close();
	}

	@Override
	public int available() throws IOException {
		return readFrom.available();
	}

	@Override
	public long skip(long n) throws IOException {
		return readFrom.skip(n);
	}

	protected String[] layers = new String[] { "\n", "\n\t", "\n\t\t", "\n\t\t\t", "\n\t\t\t\t", "\n\t\t\t\t\t",
			"\n\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t\t" };

	@Override
	public String toString() {
		return layerInfo(0);
	}

	public String infoName() {
		return this.getClass().getName();
	}

	public String layerInfo(int level) {
		StringBuilder b = new StringBuilder();
		if (level != 0)
			b.append(layers[level % layers.length]);
		b.append(infoName());

		if (readFrom instanceof WseInputStream) {
			b.append(((WseInputStream) readFrom).layerInfo(level + 1));
		} else {
			b.append(layers[(level + 1) % layers.length] + readFrom.getClass().getName());
		}

		return b.toString();
	}

	@Override
	public InputStream asInputStream() {
		return this;
	}
	
	public void disableInputLogging() {
		if (readFrom instanceof WseInputStream)
			((WseInputStream) readFrom).disableInputLogging();
	}
	
	public static void disableInputLogging(InputStream input) {
		if (input instanceof WseInputStream)
			((WseInputStream) input).disableInputLogging();
	}
}
