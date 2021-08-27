package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public class XOROutputStream extends WseOutputStream {

	private byte[] key;
	private int pointer = 0;

	public XOROutputStream(OutputStream writeTo, byte[] key) {
		super(writeTo);
		this.key = key;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public void reset() {
		this.pointer = 0;
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b ^ key[pointer % 4]);
		pointer++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (len <= 0)
			return;
		for (int i = 0; i < len; i++) {
			b[off + i] ^= key[(pointer++) % key.length];
		}
		super.write(b, off, len);
	}

}
