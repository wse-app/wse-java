package wse.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import wse.utils.LinkedByteArray;
import wse.utils.stream.LimitedInputStream;

public class StreamUtils {
	
	/**
	 * Read from inputstream and write to outputstream
	 * 
	 * @param from     the InputStream to read from
	 * @param to       the OutputStream to write to
	 * @param buffsize the maximum number of bytes to read each InputStream.read();
	 * @return the number of bytes read and written
	 * @throws IOException
	 */
	public static int write(InputStream from, OutputStream to, int buffsize) throws IOException {
		if (buffsize == 0) return 0;
		byte[] buff = new byte[buffsize];
		int p = 0;
		int c;
		while (true) {
			c = from.read(buff);
			if (c == -1)
				break;
			if (to != null)
				to.write(buff, 0, c);
			p += c;
		}
		return p;
	}

	/**
	 * Read from inputstream and write to outputstream, with long limit
	 * 
	 * @param from     the InputStream to read from
	 * @param to       the OutputStream to write to
	 * @param buffsize the maximum number of bytes to read each InputStream.read();
	 * @param limit    the limit in bytes to read and write
	 * @return the number of bytes read and written
	 * @throws IOException
	 */
	public static int write(InputStream from, OutputStream to, int buffsize, long limit) throws IOException {
		return write(new LimitedInputStream(from, limit), to, buffsize);
	}

	public static int write(InputStream from, byte[] to) throws IOException {
		return write(from, to, 0, to.length);
	}
	
	/**
	 * Read from inputstream and output into b
	 * 
	 * @param from   the InputStream to read from
	 * @param to     the array to insert read bytes into
	 * @param offset array write offset
	 * @param maxlen maximum number of bytes to write
	 * @return
	 * @throws IOException
	 */
	public static int write(InputStream from, byte[] to, int offset, int maxlen) throws IOException {
		if (maxlen == 0) return 0;
		int p = 0;
		int c;
		while (true) {
			c = from.read(to, offset + p, maxlen - p);
			if (c <= 0)
				break;
			p += c;
		}
		return p;
	}

	public static byte[] readAll(InputStream stream) throws IOException {
		LinkedByteArray arr = new LinkedByteArray();

		byte[] buff = new byte[32768];
		int p = 0;
		int a;
//		while ((a = stream.read(buff, p, buff.length - p)) != -1) {}
		while(true) {
			a = stream.read(buff, p, buff.length - p);
//			log.finest("read() " + a);
			if (a == -1) break;
			p += a;
			if (p >= buff.length) {
				arr.append(buff);
				buff = new byte[buff.length];
				p = 0;
			}
		}
		if (p > 0) {
			arr.append(buff, 0, p - 1);
		}

		return arr.combine();
	}

	public static byte[] readAll(InputStream stream, int maxLength) throws IOException {
		return readAll(new LimitedInputStream(stream, maxLength));
	}

}
