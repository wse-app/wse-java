package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SplittingInputStream extends WseInputStream {

	private OutputStream[] writeTo;
	private boolean propagateClose = false;

	public SplittingInputStream(InputStream original, OutputStream... writeTo) {
		super(original);
		this.writeTo = writeTo;
	}

	public SplittingInputStream propagateClose(boolean propagateClose) {
		this.propagateClose = propagateClose;
		return this;
	}

	@Override
	public int read() throws IOException {
		int res = super.read();
		if (res != -1) {
			for (OutputStream os : writeTo)
				os.write(res);
		} else {
			for (OutputStream os : writeTo)
				os.flush();
		}
		return res;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int res = super.read(b, off, len);
		if (res > 0) {
			for (OutputStream os : writeTo)
				os.write(b, off, res);
		} else {
			for (OutputStream os : writeTo)
				os.flush();
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		flush();
		super.close();
		if (this.propagateClose) {
			for (OutputStream os : writeTo)
				os.close();
		}
	}

	public void flush() throws IOException {
		for (OutputStream os : writeTo)
			os.flush();
	}

	@Override
	public String layerInfo(int level) {
		StringBuilder b = new StringBuilder();
		b.append(super.layerInfo(level));

		for (OutputStream os : this.writeTo) {
			if (os instanceof WseOutputStream) {
				b.append(((WseOutputStream) os).layerInfo(level + 1));
			} else {
				b.append(layers[(level + 1) % layers.length] + os.getClass().getName());
			}

		}

		return b.toString();
	}
	
	@Override
	public void disableInputLogging() {
		super.disableInputLogging();
		
		for (OutputStream output : writeTo) {
			WseOutputStream.disableOutputLogging(output);
		}
		
	}
}
