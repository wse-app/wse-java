package wse.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedInputStream extends WseInputStream {

	private byte[] clear;
	protected int bufferSize;
	protected byte[][] data;
	protected int ptr = 0;
	protected int available = 0;

	protected boolean gotEndOfStream = false;

	public BufferedInputStream(InputStream readFrom, int bufferSize) {
		this(readFrom, bufferSize, 1);
	}

	public BufferedInputStream(InputStream readFrom, int bufferSize, int nrOfBuffers) {
		super(readFrom);

		if (nrOfBuffers * bufferSize >= Integer.MAX_VALUE)
			throw new IllegalArgumentException("Too big buffer size: " + (bufferSize * nrOfBuffers));
		this.data = new byte[nrOfBuffers][bufferSize];
		this.bufferSize = bufferSize;
		this.clear = new byte[bufferSize];
	}

	protected void shiftSilent() {
		byte[] b = data[0];
		for (int i = 1; i < data.length; i++) {
			data[i - 1] = data[i];
		}
		data[data.length - 1] = b;
		System.arraycopy(clear, 0, b, 0, clear.length);
	}

	protected void shift() {
		shiftSilent();

		ptr -= bufferSize;
		if (ptr < 0)
			ptr = 0;
	}

	@Override
	public int read() throws IOException {
		while (ptr >= bufferSize) {
			shift();
		}

		fill();

		if (available == 0) {
			return -1;
		}

		int i = data[0][ptr];
		ptr++;
		available--;
		return i;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		while (ptr >= bufferSize) {
			shift();
		}

		fill();

		if (available == 0) {
			return -1;
		}

		int leftInCurrent = Math.min(available, bufferSize - ptr);
		if (len >= leftInCurrent) {
			System.arraycopy(data[0], ptr, b, off, leftInCurrent);
			ptr += leftInCurrent;
			available -= leftInCurrent;
			return leftInCurrent;
		} else {
			System.arraycopy(data[0], ptr, b, off, len);
			ptr += len;
			available -= len;
			return len;
		}
	}

	protected void fill() throws IOException {
//		System.out.println("Trying to fill");
		int first = (ptr + available) / bufferSize;
		int i = first, read = (ptr + available) % bufferSize, a = 0;
//		System.out.println(this.readFrom);
		for (; i < data.length; i++, read = 0, a = 0) {
//			System.out.println("reading 1...");
			while ((a = super.read(data[i], read, bufferSize - read)) > 0) {
				read += a;
				available += a;
//				System.out.println("reading 2... got: " + a);
			}
//			System.out.println("done reading...");
			if (a == -1) {
//				System.err.println("Got end of stream");
				this.gotEndOfStream = true;
				break;
			}
		}
	}

	/**
	 * Returns the number of bytes available before the first occurance of this
	 * subsequence
	 * 
	 * @param sub
	 * @return
	 */
	public int find(byte[] sub) {

		if (sub.length > bufferSize)
			return -1;

		int[] at = null;
		int quick_increase = Math.max(3, sub.length / 5);

		boolean foundLast = false;
		int lastOffset = 0;
		int start = ptr;

		for (int i = 0; i < data.length; i++, start = 0) {
			if (foundLast) {

//				System.out.println("found start of \"" + new String(sub) + "\" in \"" + new String(data[i-1]) + "\", " + lastOffset );

				boolean equals = true;
				for (int k = 0; k < sub.length - lastOffset; k++) {
//					System.out.println("looking for '" + (char)sub[k + lastOffset] + "' at " + k + ": '" + (char) data[i][k] + "'");
					if (data[i][k] != sub[k + lastOffset]) {
						equals = false;
						break;
					}
				}
				if (equals) {
					at = new int[] { i - 1, bufferSize - lastOffset };
					break;
				}
			}
			int until = Math.max(0, Math.min(bufferSize - sub.length, start + available - bufferSize * i - sub.length));
//			System.out.println("until: " + until + ", start: " + start + ", av: " + available);
			for (int j = start; j <= until; j++) {
//				if (j == start) System.out.println("looking at: \"" + new String(data[i], j, 5) + "\"");
				boolean equals = true;
				// quick check

				for (int k = 0; k < sub.length; k += quick_increase) {
					if (data[i][j + k] != sub[k]) {
						equals = false;
						break;
					}
				}
				if (!equals)
					continue;

				// make sure
				for (int k = 0; k < sub.length; k++) {
					if (data[i][j + k] != sub[k]) {
						equals = false;
						break;
					}
				}

				if (!equals)
					continue;

				// found
				at = new int[] { i, j };
				break;
			}

			if (at != null) {
				break;
			}
			if (i != data.length - 1) {
				foundLast = false;
				lastOffset = 0;
//				System.out.println("until: " + until);
				for (int j = until; j < bufferSize; j++) {
					boolean equals = true;
					for (int k = 0; k < Math.min(sub.length, bufferSize - j); k++) {
//						System.out.println("looking for '" + (char)sub[k] + "' at " + j + "+" + k + ": '" + (char) data[i][j+k] + "'");
						if (data[i][j + k] != sub[k]) {
							equals = false;
							break;
						}
					}
					if (equals) {
//						System.out.println("found start");
						foundLast = true;
						lastOffset = bufferSize - j;
						break;
					}
				}
			}
		}

		if (at == null)
			return -1;

		return (at[0] * bufferSize + at[1]) - ptr;
	}

	public String getData() {
		StringBuilder builder = new StringBuilder();
		builder.append("\"").append(new String(data[0])).append("\"");
		for (int i = 1; i < data.length; i++) {
			builder.append(", \"").append(new String(data[i])).append("\"");
		}
		return builder.toString();
	}

	public static class BufferedInData extends ByteArrayInputStream {
		public BufferedInData(byte[] buf) {
			super(buf);
		}

		public BufferedInData(byte[] buf, int offset, int length) {
			super(buf, offset, length);
		}

		public static BufferedInData fillBuffer(int buffSize, InputStream readFrom) throws IOException {
			byte[] buffer = new byte[buffSize];
			int read = 0;
			int a = 0;
			while ((a = readFrom.read(buffer, read, buffer.length - read)) > 0) {
				read += a;
			}
			if (read == 0) {
				return null;
			}
			return new BufferedInData(buffer, 0, read);
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public void mark(int readAheadLimit) {
		}
	}

	@Override
	public int available() {
		return gotEndOfStream ? available : Math.max(1, available);
	}
}
