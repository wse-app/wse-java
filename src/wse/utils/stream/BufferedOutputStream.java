package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public class BufferedOutputStream extends WseOutputStream {

	private byte[] permanentPrefix;
	private byte[] permanentSuffix;

	/**
	 * <pre>
	 * <code>
	 * | off | len | suffix |
	 * </code>
	 * </pre>
	 * 
	 */
	protected byte[] buffer;

	protected final int buffer_offset;
	protected final int buffer_length;
	protected final int suffix_length;

	private int counter;
	private boolean forceBufferSize = false;

	public BufferedOutputStream(OutputStream writeTo, int contentSize) {
		this(writeTo, contentSize, false);
	}

	public BufferedOutputStream(OutputStream writeTo, int contentSize, boolean forceBufferSize) {
		this(writeTo, contentSize, 0, 0);
		forceBufferSize(forceBufferSize);
	}

	public BufferedOutputStream(OutputStream writeTo, int contentSize, int prefixSize, int suffixSize) {
		super(writeTo);

		this.buffer = new byte[contentSize + prefixSize + suffixSize];
		this.buffer_offset = prefixSize;
		this.buffer_length = contentSize;
		this.suffix_length = suffixSize;
		this.counter = 0;
	}

	public void setPermanentPrefix(byte[] prefix_data) {
		if (prefix_data == null) {
			this.permanentPrefix = null;
		} else if (prefix_data.length > this.buffer_offset) {
			throw new IndexOutOfBoundsException("Prefix size can't be bigger than buffer offset");
		} else {
			this.permanentPrefix = prefix_data;
		}
	}

	public void setPermanentSuffix(byte[] suffix_data) {
		if (suffix_data == null) {
			this.permanentSuffix = null;
		} else if (suffix_data.length > this.buffer_offset) {
			throw new IndexOutOfBoundsException(
					"Suffix size is bigger than 'buffer length - prefix size - content size'");
		} else {
			this.permanentSuffix = suffix_data;
		}
	}

	/** Returns this instance with modified property forceBufferSize */
	public BufferedOutputStream forceBufferSize(boolean forceBufferSize) {
		this.forceBufferSize = forceBufferSize;
		return this;
	}

	/** Returns the current length of data stored in buffer */
	public int length() {
		return counter;
	}

	@Override
	public void write(int b) throws IOException {
		total_write++;
		buffer[buffer_offset + counter] = (byte) b;
		counter++;
		if (buffer_length - counter <= 0) {
			finish();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		total_write += len;
		int available = buffer_length - counter;

		if (available > len) {
			System.arraycopy(b, off, buffer, counter + buffer_offset, len);
			counter += len;
			return;
		}

		System.arraycopy(b, off, buffer, counter + buffer_offset, available);
		counter += available;
		off += available;
		len -= available;

		finish();

		if (buffer_offset == 0 && suffix_length == 0 && !this.forceBufferSize) {
			writeBuffer(b, off, len);
			return;
		} else {
			while (len >= buffer_length) {

				System.arraycopy(b, off, buffer, buffer_offset, buffer_length);
				counter += buffer_length;
				off += buffer_length;
				len -= buffer_length;
				finish();
			}

			if (len > 0) {
				System.arraycopy(b, off, buffer, buffer_offset, len);
				counter += len;
				return;
			}
		}
	}

	@Override
	public void flush() throws IOException {
		finish();
		super.flush();
	}

	protected final void finish() throws IOException {
		int content_offset = buffer_offset;
		int content_length = this.counter, prefix_length = 0, suffix_length = 0;
		if (this.permanentSuffix != null) {
			System.arraycopy(this.permanentSuffix, 0, buffer, buffer_offset + counter, this.permanentSuffix.length);
			suffix_length = this.permanentSuffix.length;
		}

		if (this.permanentPrefix != null) {
			System.arraycopy(this.permanentPrefix, 0, buffer, buffer_offset - this.permanentPrefix.length,
					this.permanentPrefix.length);
			prefix_length = this.permanentPrefix.length;
		}
		writeBuffer(buffer, content_offset, prefix_length, content_length, suffix_length);
		counter = 0;
	}

	/**
	 * Writes the data buffer to the next stream. It is not safe for an
	 * implementation to store the data object for later as parts might be
	 * overwritten by the buffered output stream
	 * 
	 * @param data
	 * @param content_offset
	 * @param prefix_length
	 * @param content_length
	 * @param suffix_length
	 * @throws IOException
	 */
	protected void writeBuffer(byte[] data, int content_offset, int prefix_length, int content_length,
			int suffix_length) throws IOException {
		writeBuffer(data, content_offset - prefix_length, prefix_length + content_length + suffix_length);
	}

	/**
	 * Writes the data buffer to the next stream. It is not safe for an
	 * implementation to store the data object for later as parts might be
	 * overwritten by the buffered output stream
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	protected void writeBuffer(byte[] data, int offset, int length) throws IOException {
		writeTo.write(data, offset, length);
	}

	/**
	 * Util method, can be used to write the current buffered data to a third-party
	 * OutputStream This will not affect the output for the target output as the
	 * content of the buffered outputstream remains unchanged by this method.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void writeBuffer(OutputStream out) throws IOException {
		out.write(buffer, buffer_offset, counter);
	}

}
