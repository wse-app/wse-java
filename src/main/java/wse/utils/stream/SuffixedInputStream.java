package wse.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class SuffixedInputStream extends BufferedInputStream {
	private byte[] suffix;

	public SuffixedInputStream(InputStream readFrom, int bufferSize, byte[] suffix) {
		super(readFrom, bufferSize, 2);
		this.suffix = suffix;
	}

	@Override
	public void setTarget(InputStream target) {
		super.setTarget(target);
	}

	private boolean found = false;

	private int before_skip = -1;
	private int skip = 0;

	private boolean justSkipped = false;

	private boolean continueAfterBlock = true;
	private boolean blockContinue;

	public void setContinueAfterBlock(boolean continueAfterBlock) {
		this.continueAfterBlock = continueAfterBlock;
	}

	@Override
	public int read() throws IOException {

		if (!continueAfterBlock && justSkipped && !blockContinue) {
			return -1;
		}
		blockContinue = false;
		justSkipped = false;
		while (ptr >= bufferSize) {
			shift();
		}

		if (found) {

			if (before_skip > 0) {
				int i = data[0][ptr];
				ptr++;
				available--;
				before_skip--;

				if (before_skip == 0)
					justSkipped = true;
				return i;
			}

			ptr += skip;
			available -= skip;
			skip = 0;
			found = false;
			return read();
		} else {

			fill();
			if (found)
				return read();

			if (available == 0) {
				return -1;
			}

			int i = data[0][ptr];
			ptr++;
			available--;
			return i;
		}
	}

	public boolean endOfBlock() {
		return justSkipped || (available == 0 && gotEndOfStream);
	}

	public boolean endOfData() {
		return available == 0 && gotEndOfStream;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (!continueAfterBlock && justSkipped && !blockContinue) {
			return -1;
		}
		blockContinue = false;
		justSkipped = false;
		while (ptr >= bufferSize) {
			shift();
		}

		if (found) {

			if (before_skip > 0) {
				int leftInCurrent = Math.min(available, bufferSize - ptr);
				int maxWrite = Math.min(leftInCurrent, before_skip);
//				System.err.println(before_skip);
				if (maxWrite > len) {
					System.arraycopy(data[0], ptr, b, off, len);
					ptr += len;
					available -= len;
					before_skip -= len;
					return len;
				} else {
//					System.out.println("###### " + maxWrite);
					System.arraycopy(data[0], ptr, b, off, maxWrite);
					ptr += maxWrite;
					before_skip -= maxWrite;
//					System.out.println("###### Skipping with " + before_skip + " left");
					available -= maxWrite;
//					if (before_skip == 0)
					justSkipped = true;
					return maxWrite;
				}

			}
			ptr += skip;
			available -= skip;
			found = false;
			skip = 0;
			before_skip = -1;
			return read(b, off, len);

		} else {

			fill();
			if (found) {
				return read(b, off, len);
			}

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
	}

	@Override
	protected void fill() throws IOException {
		int prev = this.available;
		super.fill();

		if (this.available - prev != 0) {
//			System.out.println("Data: " + this.getData());
		}
//		System.err.println("Filled: " + (this.available - prev) + " at " + prev + ", " + this.available);			

		int found = find(suffix);
		if (found >= 0) {
			this.found = true;
			this.before_skip = found;
			this.skip = this.suffix.length;
//			System.out.println("FOUND AT: " + found);
		}
	}

	public void blockContinue() {
		if (justSkipped)
			this.blockContinue = true;
	}

	public void reset() {
		found = false;
	}

}
