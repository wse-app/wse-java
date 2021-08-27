package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import wse.utils.exception.SHttpEncryptionException;
import wse.utils.shttp.SKey;

public class SHttpEncryptingOutputStream extends WseOutputStream {

	private int blockSize;

	private byte[] data;
	private int counter;

	private Cipher cipher;

	public SHttpEncryptingOutputStream(SKey key, int blockSize, int bulkSize) {
		this(null, key, blockSize, bulkSize);
	}

	public SHttpEncryptingOutputStream(OutputStream writeTo, SKey key, int blockSize, int bulkSize) {
		super(writeTo);
		this.writeTo = writeTo;
		this.blockSize = blockSize;

		if (bulkSize % blockSize != 0)
			throw new IllegalArgumentException(
					"Invalid bulkSize: " + bulkSize + ", must be multiple of blockSize: " + blockSize);
		this.data = new byte[bulkSize];

		try {
			SecretKeySpec newKey = new SecretKeySpec(key.getKey(), 0, key.getKey().length, "AES");

			cipher = Cipher.getInstance("AES/ECB/NoPadding"); /// CBC/NoPadding

			cipher.init(Cipher.ENCRYPT_MODE, newKey);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new SHttpEncryptionException("Encryption failed: " + e.getMessage(), e);
		} catch (InvalidKeyException e) {
			throw new SHttpEncryptionException("Failed to encrypt: Invalid encryption key: " + e.getMessage(), e);
		}
	}

	@Override
	public void write(int b) throws IOException {
		total_write += 1;
		data[counter++] = (byte) b;
		if (counter == data.length)
			encrypt();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		total_write += len;
		int left = data.length - counter;

		if (len < left) {
			System.arraycopy(b, off, data, counter, len);
			counter += len;
			return;
		}

		System.arraycopy(b, off, data, counter, left);
		counter += left;
		off += left;
		len -= left;
		encrypt();

		while (len >= data.length) {
			encrypt(b, off, data.length);
			off += data.length;
			len -= data.length;
		}

		if (len > 0) {
			System.arraycopy(b, off, data, 0, len);
			counter = len;
		}

	};

	private void encrypt() throws IOException {
		encrypt(data, 0, counter);
	}

	private void encrypt(byte[] data, int off, int len) throws IOException {
		int rest = ((len % blockSize == 0) ? 0 : (blockSize - (len % blockSize)));

		if (rest != 0 && off + len + rest > data.length)
			throw new IllegalStateException("Could not encrypt data, invalid data size");

		len += rest;

		try {
			if (cipher.getOutputSize(len) > data.length) {
				writeTo.write(cipher.doFinal(data, off, len));
			} else {
				int l = cipher.doFinal(data, off, len, data, 0);
				writeTo.write(data, 0, l);
			}
		} catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
			throw new SHttpEncryptionException("Failed to encrypt message: " + e.getMessage(), e);
		} finally {
			counter = 0;
		}
	}

	@Override
	public void flush() throws IOException {
		encrypt();
		super.flush();
	}

	@Override
	public void close() throws IOException {
		flush();
		super.close();
	}

}
