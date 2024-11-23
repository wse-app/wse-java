package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import wse.utils.LinkedByteArray.LBAE;
import wse.utils.writable.StreamWriter;

public class LinkedByteArray implements Iterable<LBAE>, StreamWriter {

	private LBAE first;
	private LBAE last;

	public LinkedByteArray() {

	}

	public LinkedByteArray(byte[] data) {
		append(data);
	}

	/**
	 * End index is inclusive
	 */
	public LinkedByteArray(byte[] data, int start, int end) {
		append(data, start, end);
	}

	public void append(byte[] data) {
		append(data, 0, data.length - 1);
	}

	/**
	 * End index is inclusive
	 */
	public void append(byte[] data, int start, int end) {
		if (first == null) {
			first = new LBAE(data, start, end);
			last = first;
			return;
		}

		// pointing to same byte array
		if (last.data == data && last.end_index == start - 1) {
			last.end_index = end;
			return;
		}

		// Append new array
		last.next = new LBAE(data, start, end);
		this.last = last.next;
	}

	public void append(LinkedByteArray tail) {
		if (this.first == null || this.last == null) {
			this.first = tail.first;
			this.last = tail.last;
			return;
		}

		this.last.next = tail.first;
		this.last = tail.last;

	}

	public LinkedByteArray cut(int last_keep_index) {
		KV<LBAE, Integer> cut = getElementWithIndex(last_keep_index);

		if (cut.v1 == null)
			return null;

		LBAE tail = cut.v1.cut(cut.v2);

		LinkedByteArray tail_array = new LinkedByteArray();
		tail_array.first = tail;
		tail_array.last = this.last;

		this.last = cut.v1;

		return tail_array;
	}

	private KV<LBAE, Integer> getElementWithIndex(int index) {
		int s = 0;

		for (LBAE n : this) {
			if (index < (s = n.size())) {
				return new KV<LBAE, Integer>(n, index);
			}
			index -= s;
		}
		return null;

	}

	public LBAE first() {
		return first;
	}

	public LBAE last() {
		return last;
	}

	public void clear() {
		this.first = null;
		this.last = null;
	}

	public int size() {
		int size = 0;

		for (LBAE n : this) {
			size += n.size();
		}

		return size;
	}

	public int elements() {
		int counter = 0;

		for (@SuppressWarnings("unused")
		LBAE n : this)
			counter++;

		return counter;
	}

	public String toString() {
		return new String(combine());
	}

	public byte[] combine() {
		return combine(0);
	}

	public byte[] combine(int blockSize) {
		int size = size();
		if (blockSize > 1) {
			size += blockSize - (size % blockSize);
		}

		byte[] result = new byte[size];

		int pointer = 0;

		for (LBAE n : this) {
			System.arraycopy(n.data, n.start_index, result, pointer, n.size());
			pointer += n.size();
		}

		return result;
	}

	@Override
	public Iterator<LBAE> iterator() {

		return new Iterator<LBAE>() {

			LBAE current = null;

			@Override
			public void remove() {
				// Not supported
			}

			@Override
			public LBAE next() {
				if (current == null) {
					current = first;
					return current;
				}

				try {
					return current.next;
				} finally {
					current = current.next;
				}

			}

			@Override
			public boolean hasNext() {
				if (current == null) {
					return first != null;
				}

				return current.next != null;
			}
		};
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		for (LBAE n : this) {
			n.writeToStream(stream, charset);
		}
	}

	public InputStream toInputStream() {
		return new LinkedByteArrayStream(this);
	}

	public static class LBAE implements StreamWriter {
		public LBAE next;

		public byte[] data;

		public int start_index;

		public int end_index;

		public int size() {
			return end_index + 1 - start_index;
		}

		public LBAE() {
		}

		public LBAE(byte[] data) {
			this(data, 0, data.length - 1);
		}

		public LBAE(byte[] data, int start, int end_index) {
			this.data = data;
			this.start_index = start;
			this.end_index = end_index;
		}

		/**
		 * next is moved to a new LBAE which is then returned
		 * 
		 * @param index
		 * @return next if last_keep_index == size()-1; a copy of this with modified
		 *         start index and containing trail.
		 * 
		 */
		public LBAE cut(int last_keep_index) {
			try {
				if (last_keep_index == size() - 1)
					return next;

				byte[] tail_data = new byte[end_index - start_index - last_keep_index];

				System.arraycopy(data, start_index + last_keep_index + 1, tail_data, 0, tail_data.length);

				this.end_index -= tail_data.length;

				LBAE tail = new LBAE(tail_data);
				tail.next = this.next;

				return tail;

			} finally {
				next = null;
			}

		}

		@Override
		public void writeToStream(OutputStream stream, Charset charset) throws IOException {
			stream.write(data, start_index, end_index + 1 - start_index);
		}
	}

	private static class LinkedByteArrayStream extends InputStream {
		int pointer = 0;
		LBAE current;

		LinkedByteArrayStream(LinkedByteArray array) {
			current = array.first;
		}

		@Override
		public int read() throws IOException {

			if (pointer + current.start_index > current.end_index) {
				if (current.next == null)
					return -1;

				current = current.next;

				pointer = 0;
				return read();
			}

			try {
				return current.data[current.start_index + pointer];
			} finally {
				pointer++;
			}

		}

		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}

		@Override
		public int read(byte[] b, int off, int maxlen) throws IOException {

			if (current == null)
				return -1;

			int read_len = Math.min(maxlen, current.size() - pointer);

			if (read_len <= 0) {
				if (current.next == null)
					return -1;

				current = current.next;
				pointer = 0;
				return read(b, off, maxlen);
			}

			System.arraycopy(current.data, current.start_index + pointer, b, off, read_len);

			pointer += read_len;
			return read_len;

		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public void close() throws IOException {
			this.current = null;
		}
	}

	public static class KV<T, S> {
		public final T v1;
		public final S v2;

		public KV(T v1, S v2) {
			this.v1 = v1;
			this.v2 = v2;
		}
	}

}
