package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public class SplittingOutputStream extends WseOutputStream {

	private OutputStream[] copyTo;

	public SplittingOutputStream(OutputStream original, OutputStream... copyTo) {
		super(original);
		this.copyTo = copyTo;
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream os : copyTo)
			os.write(b);
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (OutputStream os : copyTo)
			os.write(b, off, len);
		super.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		for (OutputStream os : copyTo)
			os.close();
		super.close();
	}

	@Override
	public void flush() throws IOException {
		for (OutputStream os : copyTo)
			os.flush();
		super.flush();
	}

	public String layerInfo(int level) {
		StringBuilder b = new StringBuilder();
		if (level != 0)
			b.append(layers[level % layers.length]);
		b.append(infoName());

		for (OutputStream copy : copyTo) {
			if (copy instanceof WseOutputStream) {
				b.append(((WseOutputStream) copy).layerInfo(level + 1));
			} else {
				b.append(layers[(level + 1) % layers.length] + copy.getClass().getName());
			}
		}

		if (writeTo instanceof WseOutputStream) {
			b.append(((WseOutputStream) writeTo).layerInfo(level + 1));
		} else {
			b.append(layers[(level + 1) % layers.length] + writeTo.getClass().getName());
		}

		return b.toString();
	}

	@Override
	public void disableOutputLogging() {
		super.disableOutputLogging();
		for (OutputStream s : copyTo)
			if (s instanceof WseOutputStream)
				((WseOutputStream) s).disableOutputLogging();
	}

}
