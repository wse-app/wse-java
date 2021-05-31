package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class RIMInputStream extends WseInputStream {

	private int blockSize;
	private int injectionSize;
	private long counter = 0;

	public RIMInputStream(InputStream readFrom, int blockSize, int injectionSize) {
		super(readFrom);
		this.blockSize = blockSize;
		this.injectionSize = injectionSize;
	}

	@Override
	public int read() throws IOException {
		if (counter % blockSize < injectionSize) {
			counter++;
			super.skip(1);
			return read();
		}
		counter++;
		return super.read();
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

	@Override
	public void close() throws IOException {
		readFrom.close();
	}

	@Override
	public int available() throws IOException {
		return super.available() * 15 / 16;
	}
	
}
