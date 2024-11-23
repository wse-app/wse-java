package wse.utils.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Loggers {
	private static final String NL = System.getProperty("line.separator");
	private static final int NL_LENGTH = NL.length();

	private static final char[] SPACE_CHARS = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

	public static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static void hexdump(Logger logger, Level level, byte[] src) {
		hexdump(logger, level, src, 0, src.length);
	}

	public static void hexdump(Logger logger, Level level, byte[] src, int srcIndex, int length) {
		if (!logger.isLoggable(level)) {
			return;
		}
		logger.log(level, "[" + length + " bytes]");

		int len;
		int si = 0;
		while (length > 0) {
			len = Math.min(512, length);
			logger.log(level, si + "-" + (si + len - 1) + "\n" + hexdump(src, srcIndex, len, si));
			length -= len;
			srcIndex += len;
			si += len;
		}

	}

	public static String hexdump(byte[] src) {
		return hexdump(src, 0, src.length);
	}

	public static String hexdump(byte[] src, int srcIndex, int length) {
		return hexdump(src, srcIndex, length, 0);
	}

	private static String hexdump(byte[] src, int srcIndex, int length, int siOffset) {
		if (length == 0) {
			return "";
		}

		int s = length % 16;
		int r = (s == 0) ? length / 16 : length / 16 + 1;
		char[] c = new char[r * (74 + NL_LENGTH) - NL_LENGTH];
		char[] d = new char[16];
		int i;
		int si = 0;
		int ci = 0;

		boolean first = true;

		do {
			if (!first) {
				NL.getChars(0, NL_LENGTH, c, ci);
				ci += NL_LENGTH;
			}
			first = false;

			toHexChars(si + siOffset, c, ci, 5);
			ci += 5;
			c[ci++] = ':';
			do {
				if (si == length) {
					int n = 16 - s;
					System.arraycopy(SPACE_CHARS, 0, c, ci, n * 3);
					ci += n * 3;
					System.arraycopy(SPACE_CHARS, 0, d, s, n);
					break;
				}
				c[ci++] = ' ';
				i = src[srcIndex + si] & 0xFF;
				toHexChars(i, c, ci, 2);
				ci += 2;
				if (i < 0 || Character.isISOControl((char) i)) {
					d[si % 16] = '.';
				} else {
					d[si % 16] = (char) i;
				}
			} while ((++si % 16) != 0);
			c[ci++] = ' ';
			c[ci++] = ' ';
			c[ci++] = '|';
			System.arraycopy(d, 0, c, ci, 16);
			ci += 16;
			c[ci++] = '|';

		} while (si < length);

		return String.valueOf(c);
	}

	public static String toHexString(int val, int size) {
		char[] c = new char[size];
		toHexChars(val, c, 0, size);
		return String.valueOf(c);
	}

	public static String toHexString(long val, int size) {
		char[] c = new char[size];
		toHexChars(val, c, 0, size);
		return String.valueOf(c);
	}

	public static String toHexString(byte[] src, int srcIndex, int size) {
		char[] c = new char[size];
		size = (size % 2 == 0) ? size / 2 : size / 2 + 1;
		for (int i = 0, j = 0; i < size; i++) {
			c[j++] = HEX_DIGITS[(src[i] >> 4) & 0x0F];
			if (j == c.length) {
				break;
			}
			c[j++] = HEX_DIGITS[src[i] & 0x0F];
		}
		return String.valueOf(c);
	}

	public static void toHexChars(int val, char dst[], int dstIndex, int size) {
		while (size > 0) {
			int i = dstIndex + size - 1;
			if (i < dst.length) {
				dst[i] = HEX_DIGITS[val & 0x000F];
			}
			if (val != 0) {
				val >>>= 4;
			}
			size--;
		}
	}

	public static void toHexChars(long val, char dst[], int dstIndex, int size) {
		while (size > 0) {
			dst[dstIndex + size - 1] = HEX_DIGITS[(int) (val & 0x000FL)];
			if (val != 0) {
				val >>>= 4;
			}
			size--;
		}
	}
}
