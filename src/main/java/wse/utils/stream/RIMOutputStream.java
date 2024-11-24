package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * 
 * Inserts number of random bytes starting at index 0 at the beginning of every
 * block. This outputstream is not thread safe
 * 
 * @author WSE
 *
 */
public class RIMOutputStream extends WseOutputStream {

	private int counter = 0;

	private int blockSize;
	private int injectionSize;

	private static Random random = new Random();

	private byte[] ri;

	private byte[] randomInjection() {
		random.nextBytes(ri);
		return ri;
	}

	/**
	 * 
	 * Implement bulk size?? premade byte[] with pre-injected random bytes, send
	 * bulk to encryption
	 * 
	 * v v a a a a a a v v a a a a a a v v a a a a a a
	 * 
	 * x = (p % (blockSize - injectionSize)) y = (p - x) / (blockSize -
	 * injectionSize)
	 * 
	 * i = y * blockSize + x + injectionSize
	 *
	 * @param blockSize
	 * @param injectionSize
	 */

	public RIMOutputStream(int blockSize, int injectionSize) {
		this(null, blockSize, injectionSize);
	}

	public RIMOutputStream(OutputStream writeTo, int blockSize, int injectionSize) {
		super(writeTo);
		this.ri = new byte[injectionSize];
		this.blockSize = blockSize;
		this.injectionSize = injectionSize;
	}

	private byte[] single = new byte[1];

	@Override
	public void write(int b) throws IOException {
		single[0] = (byte) (b);
		write(single, 0, 1);
	}

	@Override
	public void write(byte[] part, int start, int length) throws IOException {
		this.total_write += length;
		if (length <= 0)
			return;

		int blockWrite = blockSize - injectionSize;

		if (counter == 0) {
			writeTo.write(randomInjection());
			counter = injectionSize;
		}

		if (length < (blockSize - counter)) {
			writeTo.write(part, start, length);
			counter += length;
			return;
		}
		int w = blockSize - counter;
		writeTo.write(part, start, w);
		start += w;
		length -= w;
		counter = 0;

		while (length >= blockWrite) {

			writeTo.write(randomInjection());
			writeTo.write(part, start, blockWrite);
			length -= blockWrite;
			start += blockWrite;

		}

		if (length > 0) { // length is less than blockWrite
			writeTo.write(randomInjection());
			counter += injectionSize;
			writeTo.write(part, start, length);
			counter += length;
			return;
		}
	}
}
