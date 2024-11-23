package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class XORInputStream extends WseInputStream {

	private byte[] key;
	private int pointer = 0;

	public XORInputStream(InputStream readFrom, byte[] key) {
		super(readFrom);
		this.key = key;
	}

	public byte[] getKey() {
		return key;
	}

	@Override
	public int read() throws IOException {

		int i = super.read();
		if (i != -1) {
			return i ^ key[(pointer++) % key.length];
		}
		return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = super.read(b, off, len);
		if (read <= 0)
			return read;
		for (int i = 0; i < len; i++) {
			b[off + i] ^= key[(pointer++) % key.length];
		}
		return read;

	}
}
