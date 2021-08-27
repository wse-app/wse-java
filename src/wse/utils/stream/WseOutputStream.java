package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public abstract class WseOutputStream extends OutputStream {

	protected OutputStream writeTo;
	protected long total_write;

	public WseOutputStream(OutputStream writeTo) {
		super();
		this.writeTo = writeTo;
	}

	public void setTarget(OutputStream target) {
		this.writeTo = target;
	}

	public OutputStream getTarget() {
		return this.writeTo;
	}

	@Override
	public void write(int b) throws IOException {
		total_write++;
		if (writeTo != null)
			writeTo.write(b);
	}

	@Override
	public final void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		total_write += len;
		if (writeTo != null)
			writeTo.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		if (writeTo != null)
			writeTo.close();
	}

	@Override
	public void flush() throws IOException {
		if (writeTo != null)
			writeTo.flush();
	}

	protected String[] layers = new String[] { "\n", "\n\t", "\n\t\t", "\n\t\t\t", "\n\t\t\t\t", "\n\t\t\t\t\t",
			"\n\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t\t",
			"\n\t\t\t\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t\t\t\t" };

	@Override
	public String toString() {
		return layerInfo(0);
	}

	public String infoName() {
		return this.getClass().getName() + " [" + total_write + "]";
	}

	public String layerInfo(int level) {
		StringBuilder b = new StringBuilder();
		if (level != 0)
			b.append(layers[level % layers.length]);
		b.append(infoName());

		if (writeTo != null) {
			if (writeTo instanceof WseOutputStream)
				b.append(((WseOutputStream) writeTo).layerInfo(level + 1));
			else
				b.append(layers[(level + 1) % layers.length] + writeTo.getClass().getName());
		}

		return b.toString();
	}

	public void reset() {
		this.total_write = 0;
	}

	/**
	 * Disables all LoggingOutputStreams in the output stack
	 */
	public void disableOutputLogging() {
		if (writeTo instanceof WseOutputStream)
			((WseOutputStream) writeTo).disableOutputLogging();
	}
}
