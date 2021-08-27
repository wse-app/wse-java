package wse.utils.writable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class SizeCatcher extends OutputStream {

	int size;

	@Override
	public void write(int b) throws IOException {
		size += 1;
	}

	@Override
	public void write(byte[] b) {
		size += b.length;
	}

	@Override
	public void write(byte[] b, int off, int len) {
		size += len;
	}

	public int getSize() {
		return size;
	}

	public int discard() {
		int s = size;
		size = 0;
		return s;
	}

	public static int getSize(Charset charset, StreamWriter... wa) {
		SizeCatcher catcher = new SizeCatcher();
		try {
			for (StreamWriter w : wa)
				w.writeToStream(catcher, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return catcher.getSize();
	}

}
