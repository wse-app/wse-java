package wse.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class StringUtils {
	private StringUtils() {
	}

	public static String stack(String piece, int times) {
		if (times < 1)
			return "";

		char[] result = new char[piece.length() * times];
		char[] src = piece.toCharArray();

		for (int i = 0; i < times; i++) {
			System.arraycopy(src, 0, result, i * src.length, src.length);
		}
		return new String(result);
	}

	public static String[] split(String source, char s, PrefixGroup... ignore) {
		if (ignore == null)
			ignore = new PrefixGroup[0];

		ArrayList<Integer> splitPoints = new ArrayList<Integer>();
		String split = s + "";
		Map<Integer, Integer> ignored = new HashMap<Integer, Integer>();

		for (int i = 0; i < ignore.length; i++)
			ignored.put(i, 0);

		for (int index = 0; index < source.length(); index++) {
			int[] next = process(source, index, ignore);

			int add = next[0];
			int groupId = next[1];
			int ig = 0;

			if (add != 0) {
				ig = ignored.get(groupId);

				boolean already = ig > 0;
				PrefixGroup group = ignore[groupId];

				if (add > 0 || ignored.get(groupId) > 0) {

					if (group.same(next[2]) && already) {
						add *= -1;
					}

					ig += add;
				}
				ignored.put(groupId, ig);
			}

			int tig = 0;
			for (int d : ignored.values())
				tig += d;

			if (tig == 0 && nextEquals(source, index, split)) {
				splitPoints.add(index);
			}
		}
		Integer[] sp = new Integer[splitPoints.size()];
		splitPoints.toArray(sp);
		int[] splits = ArrayUtils.convert(sp);

		return split(source, splits, split);
	}

	private static int[] process(String source, int index, PrefixGroup... any) {
		int[] result = new int[3];

		int add = 0;
		int groupId = 0;
		int prefixId = 0;

		for (PrefixGroup group : any) {
			boolean breaked = false;
			for (String st : group.startDef) {
				if (source.substring(index, index + st.length()).equals(st)) {
					add = 1;
					breaked = true;
					break;
				}
				groupId++;
				prefixId++;
			}
			if (!breaked) {
				groupId = 0;
				prefixId = 0;
				for (String en : group.endDef) {
					if (source.substring(index, index + en.length()).equals(en)) {
						add = -1;
						break;
					}
					groupId++;
					prefixId++;
				}
				if (groupId != 0) {
					groupId--;
					prefixId--;
				}
			}
		}
		result[0] = add;
		result[1] = groupId;
		result[2] = prefixId;
		return result;
	}

	public static String WS_collapse(String value) {
		if (value == null)
			return null;
		return value.trim().replaceAll("\\s+", " ");

	}

	public static String WS_replace(String value) {
		if (value == null)
			return null;
		return value.replaceAll("\\s", " ");
	}

	public static String[] split(String source, String split) {
		return split(source, split, split);
	}

	public static String[] split(String source, String regex, int max) {
		return split(source, null, regex, max);
	}

	public static String[] split(String source, String containsCheck, String regex) {
		return split(source, containsCheck, regex, -1);
	}

	public static String[] split(String source, String containsCheck, String regex, int max) {
		if (source == null)
			return new String[0];

		if (containsCheck != null && !source.contains(containsCheck))
			return new String[] { source };

		return source.split(regex, max);
	}

	public static List<String> split(String source, char split) {
		return split(source, split, -1);
	}

	public static List<String> split(String source, char split, int max) {
		if (source == null || source.length() == 0)
			return Collections.emptyList();

		List<String> result = new LinkedList<>();

		char[] c = source.toCharArray();

		int s = 0;
		for (int i = 0; i < c.length && (max <= 0 || result.size() < max - 1); i++) {
			if (c[i] == split) {
				if (i - s != 0)
					result.add(new String(c, s, i - s));
				s = i + 1;
			}
		}

		if (s < c.length) {
			result.add(new String(c, s, c.length - s));
		}

		return result;
	}

	public static int getIndexFromLineColumn(int line, int column, String text) {
		String[] lines = text.split("\n");
		if (lines.length < line)
			return text.length();
		line--;
		int index = 0;
		for (int i = 0; i < line; i++)
			index += lines[i].length() + 1;
		return index + column - 1;

	}

	public static String[] split(String source, int[] splitPoints, String split) {
		String[] chars = destroy(source);
		String[] result = new String[splitPoints.length + 1];

		int point = 0;
		for (int i = 0; i < chars.length; i++) {
			String now = chars[i];
			if (ArrayUtils.contains(splitPoints, i))
				point++;
			if (result[point] == null)
				result[point] = "";
			result[point] += now;
		}

		for (int i = 0; i < result.length; i++) {
			if (result[i].startsWith(split))
				result[i] = result[i].replaceFirst(split, "");
		}

		return result;
	}

	public static String flip(String source) {
		return new StringBuilder(source).reverse().toString();
	}

	public static String firstUCase(String source) {
		String lower = source.toLowerCase();
		String upper = source.substring(0, 1).toUpperCase();

		String whole = upper + lower.substring(1);
		return whole;
	}

	public static boolean isStringReadableBackwards(String source) {
		if (source == null || source.length() == 0)
			return false;
		int sl = source.length() - 1;
		int hl = source.length() / 2;

		for (int i = 0; i < hl; i++) {
			if (source.charAt(i) != source.charAt(sl - i))
				return false;
		}
		return true;
	}

	/**
	 * Returns a String array of all characters in the String
	 * 
	 * @param source
	 * @return
	 */
	public static String[] destroy(String source) {
		return clean(source.split(""));
	}

	public static String fix(String[] destroyed, boolean space) {
		String result = "";
		for (int i = 0; i < destroyed.length; i++) {
			if (destroyed[i].equals("\n"))
				result += destroyed[i];
			else {
				result += destroyed[i] + ((i != destroyed.length - 1 && space) ? " " : "");
			}
		}
		return result;
	}

	/**
	 * Checks for if next String at index equals the string specified. Example:
	 * StringUtils.nextEquals("Well, Hello World!", 6, "Hello"); will return true
	 * 
	 * @param source
	 * @param index
	 * @param equals
	 * @return
	 */
	public static boolean nextEquals(String source, int index, String equals) {
		return (source.substring(index, index + equals.length()).equals(equals));
	}

	public static int instancesOf(char c, String source) {
		int result = 0;
		for (char d : source.toCharArray())
			if (c == d)
				result++;
		return result;
	}

	/**
	 * Returns an array with size: first parameters size / second parameter
	 * Exceptions: - (first parameter size) % (second parameter) != 0: returns null
	 * 
	 * @param data
	 * @param width
	 * @return
	 */
	public static String[][] splitEvery(String[] data, int width) {
		if (data.length % width != 0)
			return null;
		int height = data.length / width;

		String[][] result = new String[width][];
		for (int x = 0; x < width; x++) {
			result[x] = new String[height];
			for (int y = 0; y < height; y++) {
				result[x][y] = data[x + y * width];
			}
		}
		return result;
	}

	public static String remove(String src, String remove) {
		return src.replaceAll(remove, "");
	}

	/**
	 * Returns a copy of the parameter, without null or empty Strings
	 * 
	 * @param data
	 * @return
	 */
	public static String[] clean(String[] data) {
		int total = 0;
		String[] result = new String[data.length];
		for (String s : data) {
			if (s == null || s.isEmpty())
				continue;
			result[total] = s;
			total++;
		}
		return getPart(result, 0, total);
	}

	/**
	 * Returns a part of an array
	 * 
	 * @param data
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */

	public static String[] getPart(String[] data, int startIndex, int endIndex) {
		if (startIndex > endIndex || startIndex < 0)
			return null;

		int size = endIndex - startIndex;
		String[] result = new String[size];

		for (int i = 0; i < size; i++)
			result[i] = data[startIndex + i];
		return result;
	}

	public static String cap(String source, int max_length, String addIfCut) {
		if (source.length() <= max_length)
			return source;
		else
			return source.substring(0, max_length - 1) + addIfCut;
	}

	public static String put(String source, String add, int index) {
		return source.substring(0, index) + add + source.substring(index, source.length());
	}

	public static int nextSpace(String source, int from) {
		return source.indexOf(" ", from);
	}

	/**
	 * Returns an array with size: 2, containing all Strings in the first parameter.
	 * Exceptions: - second parameter is greater length of Strings: returns null
	 * 
	 * @param data
	 * @param first
	 * @return
	 */
	public static String[][] splitOn2(String[] data, int first) {
		if (first > data.length)
			return new String[][] { data };

		String[] fi = new String[first];
		String[] en = new String[data.length - first];

		for (int i = 0; i < data.length; i++) {
			if (i < first)
				fi[i] = data[i];
			else
				en[i - first] = data[i];
		}
		return new String[][] { fi, en };
	}

	public static final PrefixGroup // Default groups
	STRING = new PrefixGroup("\"", "\""), CHAR = new PrefixGroup("'", "'"), PARANTHESIS = new PrefixGroup("(", ")"),
			CURLY_BRACKET = new PrefixGroup("{", "}"), SQUARE_BRACKET = new PrefixGroup("[", "]"),
			LESS_BIGGER_THAN = new PrefixGroup("<", ">");

	public static final PrefixGroup CODE = new PrefixGroup(STRING, PARANTHESIS, CURLY_BRACKET, SQUARE_BRACKET,
			LESS_BIGGER_THAN);

	/**
	 * PrefixGroup class Used for parameters in some of the methods contained in
	 * StringUtil.java
	 * 
	 * 
	 * @author CarlCaesar
	 *
	 */
	public static class PrefixGroup {

		private final String[] startDef;
		private String[] start;

		private final String[] endDef;
		private String[] end;

		/**
		 * Group one or more string prefixes/suffixes
		 * 
		 * Example: new PrefixGroup("&lt;", "&gt;", "(", ")", "[", "]"); As seen in the
		 * example, the format should be: start, end, start, end etc.
		 * 
		 * @param groups
		 */
		public PrefixGroup(String... groups) {
			if (groups.length % 2 != 0)
				throw new IllegalArgumentException(
						"Can't initialize a prefixgroup with different amout of prefix/suffix");
			String[][] s = StringUtils.splitEvery(groups, 2);
			startDef = s[0];
			endDef = s[1];
			start = new String[startDef.length];
			end = new String[endDef.length];

		}

		public PrefixGroup(PrefixGroup... others) {
			String[] start = new String[0];
			String[] end = new String[0];
			for (PrefixGroup o : others) {
				start = ArrayUtils.combine(start, o.startDef);
				end = ArrayUtils.combine(end, o.endDef);
			}
			this.startDef = start;
			this.endDef = end;
		}

		public boolean same(int index) {
			if (index >= endDef.length)
				return false;
			return endDef[index].equals(startDef[index]);
		}

		public String[] getStarts() {
			// Users can change the groups, so we want to reset it, before they
			// access it again
			for (int i = 0; i < startDef.length; i++)
				start[i] = startDef[i];
			return start;
		}

		public String[] getEnds() {
			// Users can change the groups, so we want to reset it, before they
			// access it again
			for (int i = 0; i < endDef.length; i++)
				end[i] = endDef[i];
			return end;
		}
	}

	/**
	 * returns -1, 0 or 1 if the first string is below, equal to, or higher than the
	 * second string.
	 * 
	 * @param first  The first string to compare
	 * @param second The second string to compare
	 * @param a_z    whether letters starting with A should come before Z, false if
	 *               Z before A
	 * @return -1, 0 or 1
	 */
	public static int compare(String first, String second, boolean a_z) {
		first = first.toLowerCase();
		second = second.toLowerCase();
		int fl = first.length();
		int sl = second.length();
		for (int i = 0;; i++) {
			if (fl < i + 1 && sl < i + 1)
				return 0;
			if (fl < i + 1)
				return 1;
			if (sl < i + 1)
				return -1;

			int f = first.charAt(i);
			int s = second.charAt(i);
			if (a_z) {
				if (f < s)
					return 1;
				if (s < f)
					return -1;
				continue;
			} else {
				if (f < s)
					return -1;
				if (s < f)
					return 1;
				continue;
			}

		}
	}

	public static String capitalize(String text) {
		if (text == null)
			return "Null";
		if (text.length() <= 1)
			return text.toUpperCase();
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

	////////////////////////////////////
	/*
	 * Code for base 64
	 */

	private static final byte[] decodeMap = initDecodeMap();
	private static final byte PADDING = 127;

	private static byte[] initDecodeMap() {
		byte[] map = new byte[128];
		int i;
		for (i = 0; i < 128; i++) {
			map[i] = -1;
		}

		for (i = 'A'; i <= 'Z'; i++) {
			map[i] = (byte) (i - 'A');
		}
		for (i = 'a'; i <= 'z'; i++) {
			map[i] = (byte) (i - 'a' + 26);
		}
		for (i = '0'; i <= '9'; i++) {
			map[i] = (byte) (i - '0' + 52);
		}
		map['+'] = 62;
		map['/'] = 63;
		map['='] = PADDING;

		return map;
	}

	private static int guessLength(String text) {
		final int len = text.length();

		// compute the tail '=' chars
		int j = len - 1;
		for (; j >= 0; j--) {
			byte code = decodeMap[text.charAt(j)];
			if (code == PADDING) {
				continue;
			}
			if (code == -1) // most likely this base64 text is indented. go with
							// the upper bound
			{
				return text.length() / 4 * 3;
			}
			break;
		}

		j++; // text.charAt(j) is now at some base64 char, so +1 to make it the
				// size
		int padSize = len - j;
		if (padSize > 2) // something is wrong with base64. be safe and go with
							// the upper bound
		{
			return text.length() / 4 * 3;
		}

		// so far this base64 looks like it's unindented tightly packed base64.
		// take a chance and create an array with the expected size
		return text.length() / 4 * 3 - padSize;
	}

	public static String applyUnicodes(String text) {
		return new String(applyUnicodes(text.getBytes()));
	}

	public static byte[] applyUnicodes(byte[] text) {
		int nr = 0;
		{
			for (byte b : text) {
				if (b == '\"' || b == '\\' || b == '/' || b == '\b' || b == '\f' || b == '\n' || b == '\r' || b == '\t'
						|| b == '\'') {
					nr++;
				}
			}
		}

		byte[] result = new byte[text.length + nr * 5];

		{
			byte b;
			byte[] d = new byte[2];
			byte[] f;
			for (int i = 0, c = 0; i < text.length; i++, c++) {
				b = text[i];
				if (b == '\"' || b == '\\' || b == '/' || b == '\b' || b == '\f' || b == '\n' || b == '\r' || b == '\t'
						|| b == '\'') {
					result[c++] = '\\';
					result[c++] = 'u';

					result[c++] = '0';
					result[c++] = '0';

					f = Integer.toHexString(b).getBytes();
					if (f.length == 1) {
						d[0] = '0';
						d[1] = f[0];
					} else if (f.length == 2) {
						d[0] = f[0];
						d[1] = f[1];
					}

					result[c++] = d[0];
					result[c] = d[1];

				} else {
					result[c] = text[i];
				}
			}
		}

		return result;
	}

	public static String parseUnicodes(String text) {
		return parseUnicodes(text.getBytes(), true);
	}

	public static String parseUnicodes(byte[] text, boolean override) {
		// Find number of occurances
		int nr = 0;

		byte[] result;
		if (override)
			result = text;
		else
			result = new byte[text.length];

		{
			byte b;
			int lvl = 0;
			String a;

			byte[] d = new byte[4];

			for (int i = 0, c = 0; i < text.length; i++, c++) {
				b = text[i];

				switch (lvl) {
				case 0:
					if (b == '\\') {
						lvl++;
						c--;
					} else {
						result[c] = text[i];
					}
					break;
				case 1:
					if (b == 'u') {
						nr++;
						lvl = 0;

						d[0] = text[i + 1];
						d[1] = text[i + 2];
						d[2] = text[i + 3];
						d[3] = text[i + 4];

						a = new String(d);

						i += 4;

						result[c] = Byte.parseByte(a, 16);
					}
					break;
				default:
					break;
				}

			}
		}
		return new String(result, 0, text.length - nr * 5);
	}

	public static byte[] parseBase64Binary(String text) {
		if (text == null)
			return null;
		final int buflen = guessLength(text);
		final byte[] out = new byte[buflen];
		int o = 0;

		final int len = text.length();
		int i;

		final byte[] quadruplet = new byte[4];
		int q = 0;

		// convert each quadruplet to three bytes.
		for (i = 0; i < len; i++) {
			char ch = text.charAt(i);
			byte v = decodeMap[ch];

			if (v != -1) {
				quadruplet[q++] = v;
			}

			if (q == 4) {
				// quadruplet is now filled.
				out[o++] = (byte) ((quadruplet[0] << 2) | (quadruplet[1] >> 4));
				if (quadruplet[2] != PADDING) {
					out[o++] = (byte) ((quadruplet[1] << 4) | (quadruplet[2] >> 2));
				}
				if (quadruplet[3] != PADDING) {
					out[o++] = (byte) ((quadruplet[2] << 6) | (quadruplet[3]));
				}
				q = 0;
			}
		}

		if (buflen == o) // speculation worked out to be OK
		{
			return out;
		}

		// we overestimated, so need to create a new buffer
		byte[] nb = new byte[o];
		System.arraycopy(out, 0, nb, 0, o);
		return nb;
	}

	public static byte[] parseHexBinary(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String printHexBinary(byte[] input) {
		return printHexBinary(input, 0, input.length);
	}

	public static String printHexBinary(byte[] input, int offset, int len) {
		char[] buf = new char[len * 2];
		printHexBinary(input, offset, len, buf, 0);
		return new String(buf);
	}

	public static void printHexBinary(byte[] input, int offset, int len, char[] target, int targetOffset) {
		for (int i = offset, j = targetOffset; i < len; i++) {
			target[j++] = Character.forDigit((input[i] >> 4) & 0xF, 16);
			target[j++] = Character.forDigit((input[i] & 0xF), 16);
		}
	}

	public static String printBase64Binary(byte[] input) {
		return printBase64Binary(input, 0, input.length);
	}

	public static String printBase64Binary(byte[] input, int offset, int len) {
		char[] buf = new char[((len + 2) / 3) * 4];
		int ptr = printBase64Binary(input, offset, len, buf, 0);
		assert ptr == buf.length;
		return new String(buf);
	}

	/**
	 * Encodes a byte array into a char array by doing base64 encoding.
	 *
	 * The caller must supply a big enough buffer.
	 *
	 * @return the value of {@code ptr+((len+2)/3)*4}, which is the new offset in
	 *         the output buffer where the further bytes should be placed.
	 */
	public static int printBase64Binary(byte[] input, int offset, int len, char[] buf, int ptr) {
		// encode elements until only 1 or 2 elements are left to encode
		int remaining = len;
		int i;
		for (i = offset; remaining >= 3; remaining -= 3, i += 3) {
			buf[ptr++] = encode(input[i] >> 2);
			buf[ptr++] = encode(((input[i] & 0x3) << 4) | ((input[i + 1] >> 4) & 0xF));
			buf[ptr++] = encode(((input[i + 1] & 0xF) << 2) | ((input[i + 2] >> 6) & 0x3));
			buf[ptr++] = encode(input[i + 2] & 0x3F);
		}
		// encode when exactly 1 element (left) to encode
		if (remaining == 1) {
			buf[ptr++] = encode(input[i] >> 2);
			buf[ptr++] = encode(((input[i]) & 0x3) << 4);
			buf[ptr++] = '=';
			buf[ptr++] = '=';
		}
		// encode when exactly 2 elements (left) to encode
		if (remaining == 2) {
			buf[ptr++] = encode(input[i] >> 2);
			buf[ptr++] = encode(((input[i] & 0x3) << 4) | ((input[i + 1] >> 4) & 0xF));
			buf[ptr++] = encode((input[i + 1] & 0xF) << 2);
			buf[ptr++] = '=';
		}
		return ptr;
	}

	public static char encode(int i) {
		return encodeMap[i & 0x3F];
	}

	private static final char[] encodeMap = initEncodeMap();

	private static char[] initEncodeMap() {
		char[] map = new char[64];
		int i;
		for (i = 0; i < 26; i++) {
			map[i] = (char) ('A' + i);
		}
		for (i = 26; i < 52; i++) {
			map[i] = (char) ('a' + (i - 26));
		}
		for (i = 52; i < 62; i++) {
			map[i] = (char) ('0' + (i - 52));
		}
		map[62] = '+';
		map[63] = '/';

		return map;
	}

	/*
	 * 
	 */
	//////////////////////////////////////

}
