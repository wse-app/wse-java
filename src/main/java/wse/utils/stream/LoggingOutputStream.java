package wse.utils.stream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.utils.log.Loggers;

public class LoggingOutputStream extends WseOutputStream {

	/**
	 * Maximum number of byte buffers to log. Default value is 4.
	 */
	public static int MAX_PARTS = 4;
	
	/**
	 * Byte buffer size. Default value is 8192.
	 */
	public static int BUFFER_SIZE = 8192;

	private static final int MIN_BUFFER_SIZE = 16;

	private final Logger logger;
	private byte[] data;
	private int counter = 0;
	private Level level;
	private String title;

	private int part = 1;
	private boolean asHex = false;
	private int maxParts = MAX_PARTS;

	private boolean disabled = false;

	public LoggingOutputStream(Logger logger, Level level, String title) {
		this(logger, level, title, false);
	}

	public LoggingOutputStream(Logger logger, Level level, String title, boolean hex) {
		super(null);
		this.logger = logger;
		this.data = new byte[Math.max(MIN_BUFFER_SIZE, BUFFER_SIZE)];
		this.level = level;
		this.title = title;
		this.asHex = hex;
	}

	public LoggingOutputStream printHex(boolean printHex) {
		asHex = printHex;
		return this;
	}

	@Override
	public void write(int b) throws IOException {
		if (disabled)
			return;
		if (part > maxParts)
			return;
		super.total_write += 1;
		data[counter++] = (byte) b;

		if (counter == data.length)
			print(counter);
	}

	private void print(int length) {
		if (disabled)
			return;
		if (maxParts > 0 && part > maxParts)
			return;
		if (length == 0)
			return;
		if (logger.isLoggable(level)) {
			if (this.asHex) {
				String hex = Loggers.hexdump(data, 0, length);
				logger.log(level, (title + " [" + length + " bytes] (pt. " + part + ")\n") + (hex));
			} else {
				logger.log(level,
						(title + " [" + length + " bytes] (pt. " + part + ")\n") + (new String(data, 0, length)) + "");
			}
			if (maxParts > 0 && part >= maxParts && length == data.length) {
				logger.log(level, title + "[...] (Maximum parts reached: " + maxParts + ")");
			}
		}
		counter = 0;
		part++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (disabled)
			return;
		if (part > maxParts)
			return;
		super.total_write += len;
		int left = data.length - counter;

		if (left >= len) {

			System.arraycopy(b, off, data, counter, len);
			counter += len;
			if (counter == data.length)
				print(counter);
			return;
		}

		System.arraycopy(b, off, data, counter, left);
		counter += left;
		off += left;
		len -= left;
		print(counter);

		int maxLen = data.length;

		while (len >= maxLen) {
			System.arraycopy(b, off, data, counter, maxLen);
			counter += maxLen;
			off += maxLen;
			len -= maxLen;
			print(counter);
			if (part > maxParts)
				return;
		}

		if (len > 0) {
			System.arraycopy(b, off, data, counter, len);
			counter += len;
		}
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		if (disabled)
			return;
		print(counter);
	}

	@Override
	public void disableOutputLogging() {
		super.disableOutputLogging();
		this.disabled = true;
	}
}
