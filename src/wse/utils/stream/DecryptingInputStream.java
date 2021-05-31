package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import wse.utils.SHttp;
import wse.utils.exception.WseSHttpDecryptionException;
import wse.utils.shttp.SKey;

public class DecryptingInputStream extends InputStream {

	private InputStream readFrom;
	private int blockSize;

	private byte[] data;
	private int available = 0;

	private int read_ptr = 0;

	private Cipher cipher;

	private boolean reached_end = false;

	public DecryptingInputStream(InputStream readFrom, SKey key, int blockSize, int bulkSize) {
		if (bulkSize % blockSize != 0)
			throw new IllegalArgumentException(
					"Invalid bulkSize: " + bulkSize + ", must be multiple of blockSize: " + blockSize);
		
		cipher = SHttp.getCipher(key, Cipher.DECRYPT_MODE);

		this.readFrom = readFrom;
		this.blockSize = blockSize;

		this.data = new byte[bulkSize];
		this.read_ptr = bulkSize;
	}

	@Override
	public int read() throws IOException {

		if (reached_end && available == 0)
			return -1;

		if (read_ptr >= data.length) {
			readData();
			if (available == 0 && reached_end)
				return -1;
			decrypt(available);
		}

		return data[read_ptr++];

	}

	@Override
	public int read(byte[] b) throws IOException {
		if (reached_end && available == 0)
			return -1;
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (reached_end && available == 0)
			return -1;
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		if (available == 0) {
			readData();
			if (reached_end && available == 0)
				return -1;
			decrypt(available);
		}

		if (available >= len) {
			System.arraycopy(data, read_ptr, b, off, len);
			read_ptr += len;
			available -= len;
			return len;
		}

		int read = available;
		System.arraycopy(data, read_ptr, b, off, read);
		read_ptr += read;
		available = 0;

		return read;
	}

	private void readData() throws IOException {
		int c = 0;
		int read;

		boolean reached_end = true;
		while ((read = readFrom.read(data, c, data.length - c)) != -1) {
			c += read;
			if (read >= data.length) {
				reached_end = false;
				break;
			}
		}

		this.reached_end = reached_end;
		if (c != -1)
			available = c;
		else
			available = 0;
		read_ptr = 0;
	}

	private void decrypt(int length) {

		if (length % blockSize != 0) {
			throw new WseSHttpDecryptionException(
					"Can't decrypt length not multiple of blockSize: " + length + " / " + blockSize);
		}

		try {
			cipher.doFinal(data, 0, length, data, 0);
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			throw new WseSHttpDecryptionException("Failed to decrypt data: " + e.getMessage(), e);
		}
	}

	public void close() throws IOException {
		readFrom.close();
	};
}
