package wse.utils.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

import wse.utils.internal.PushableReader;

public class JTokenizer {

	private static final char BEGIN_ARRAY = '[';
	private static final char BEGIN_OBJECT = '{';
	private static final char END_ARRAY = ']';
	private static final char END_OBJECT = '}';
	private static final char NAME_SEPARATOR = ':';
	private static final char VALUE_SEPARATOR = ',';
	private static final char QUOTE = '"';
	private static final char APOS = '\'';

	private final PushableReader reader;

	@SuppressWarnings("unchecked")
	public static <T> T parse(InputStream input, Charset cs) throws IOException {
		JTokenizer tokenizer = new JTokenizer(input, cs);
		return (T) tokenizer.beginValue();
	}

	protected JTokenizer(InputStream input, Charset cs) {
		this(new InputStreamReader(input, cs));
	}

	protected JTokenizer(Reader reader) {
		this.reader = new PushableReader(reader);
	}

	private Object beginValue() throws IOException {
		char c = nextSkipWS();

		switch (c) {
		case BEGIN_ARRAY: {
			return beginArray();
		}
		case BEGIN_OBJECT: {
			return beginObject();
		}
		case APOS:
		case QUOTE:
			return beginString(c);
		default: {
			reader.push(c);

			// Read name or number
			return beginUnquoted();
		}
		}
	}

	private Object beginUnquoted() throws IOException {
		StringBuilder sb = new StringBuilder();

		char c = (char) reader.read();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = (char) reader.read();
		}
		if (!reader.end()) {
			reader.push(c);
		}

		String unquoted = sb.toString().trim();
		if ("".equals(unquoted)) {
			throw this.syntaxError("Missing value");
		}

		return stringToValue(unquoted);
	}

	private static Object stringToValue(String text) {
		if (text == null || text.isEmpty())
			return null;
		if ("true".equals(text))
			return Boolean.TRUE;
		if ("false".equals(text))
			return Boolean.FALSE;
		if ("null".equals(text))
			return null;

		char initial = text.charAt(0);
		if ((initial >= '0' && initial <= '9') || initial == '-' || initial == '.') {
			try {
				return stringToNumber(text);
			} catch (Exception ignore) {
			}
		}
		return text;
	}

	private static boolean isDecimalNotation(final String val) {
		return val.indexOf('.') > -1 || val.indexOf('e') > -1 || val.indexOf('E') > -1 || "-0".equals(val);
	}

	private static Number stringToNumber(String val) {
		char initial = val.charAt(0);
		if ((initial >= '0' && initial <= '9') || initial == '-') {
			// decimal representation
			if (isDecimalNotation(val)) {
				// Use a BigDecimal all the time so we keep the original
				// representation. BigDecimal doesn't support -0.0, ensure we
				// keep that by forcing a decimal.
				try {
					BigDecimal bd = new BigDecimal(val);
					if (initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0) {
						return Double.valueOf(-0.0);
					}
					return bd;
				} catch (NumberFormatException retryAsDouble) {
					// this is to support "Hex Floats" like this: 0x1.0P-1074
					try {
						Double d = Double.valueOf(val);
						if (d.isNaN() || d.isInfinite()) {
							throw new NumberFormatException("val [" + val + "] is not a valid number.");
						}
						return d;
					} catch (NumberFormatException ignore) {
						throw new NumberFormatException("val [" + val + "] is not a valid number.");
					}
				}
			}
			// block items like 00 01 etc. Java number parsers treat these as Octal.
			if (initial == '0' && val.length() > 1) {
				char at1 = val.charAt(1);
				if (at1 >= '0' && at1 <= '9') {
					throw new NumberFormatException("val [" + val + "] is not a valid number.");
				}
			} else if (initial == '-' && val.length() > 2) {
				char at1 = val.charAt(1);
				char at2 = val.charAt(2);
				if (at1 == '0' && at2 >= '0' && at2 <= '9') {
					throw new NumberFormatException("val [" + val + "] is not a valid number.");
				}
			}

			BigInteger bi = new BigInteger(val);
			if (bi.bitLength() <= 31) {
				return Integer.valueOf(bi.intValue());
			}
			if (bi.bitLength() <= 63) {
				return Long.valueOf(bi.longValue());
			}
			return bi;
		}
		throw new NumberFormatException("val [" + val + "] is not a valid number.");
	}

	private JObject beginObject() throws IOException {

		char next;
		JObject obj = new JObject();
		obj.setPos(getRow(), getColumn());

		while (true) {

			next = nextSkipWS();

			switch (next) {
			case END_OBJECT:
				return obj;
			case QUOTE: // fall through
			case APOS:
				beginMember(obj, next);
				continue;
			case VALUE_SEPARATOR:
				continue;
			default:
				throw syntaxError("Expected ',|\"|}', found '" + next + "'");

			}

		}
	}

	private void beginMember(JObject obj, char c) throws IOException {

		String key = beginString(c);

		char next = nextSkipWS();

		if (next != NAME_SEPARATOR) {
			throw syntaxError("Expected key-value separator ':', found '" + next + "'");
		}

		Object value = beginValue();

		obj.put(key, value);
	}

	private JArray beginArray() throws IOException {
		char next;
		JArray arr = new JArray();

		entries: while (true) {
			next = nextSkipWS();

			if (next == END_ARRAY) {
				return arr;
			}

			reader.push(next);

			Object o = beginArrayEntry();
			arr.add(o);

			next = nextSkipWS();

			switch (next) {
			case VALUE_SEPARATOR:
				continue entries;
			case END_ARRAY:
				return arr;
			default: {
				throw syntaxError("Expected ',|]' but found '" + next + "'");
			}
			}
		}
	}

	private Object beginArrayEntry() throws IOException {
		return beginValue();
	}

	private String beginString(char end) throws IOException {
		char c;
		StringBuilder sb = new StringBuilder();
		for (;;) {
			c = next();
			switch (c) {
			case 0:
			case '\n':
			case '\r':
				if (end == 0) return sb.toString();
				throw this.syntaxError("Unterminated string");
			case '\\':
				c = next();
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'u':
					try {
						sb.append((char) Integer.parseInt(this.next(4), 16));
					} catch (NumberFormatException e) {
						throw this.syntaxError("Illegal escape.", e);
					}
					break;
				case '"':
				case '\'':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					throw this.syntaxError("Illegal escape.");
				}
				break;
			default:
				if (c == end) {
					return sb.toString();
				}
				sb.append(c);
			}
		}
	}

	/**
	 * @param input the input stream
	 * @return the next character in the input stream that is not a whitespace
	 *         character
	 * @throws IOException If an I/O error occurs
	 */
	private char nextSkipWS() throws IOException {
		int c;
		while (true) {
			c = reader.read();

			switch (c) {
			case 0x20: // Space
			case 0x0D: // CR
			case 0x0A: // NL
			case 0x09: // Tab
				continue;
			}
			return (char) c;
		}
	}

	private char next() throws IOException {
		return (char) reader.read();
	}

	private String next(int n) throws IOException {
		if (n == 0) {
			return "";
		}

		char[] chars = new char[n];
		int pos = 0;

		while (pos < n) {
			chars[pos] = this.next();
			if (reader.end()) {
				throw this.syntaxError("Unexpected end of stream");
			}
			pos += 1;
		}
		return new String(chars);
	}

	private JException syntaxError(String message) {
		return syntaxError(message, null);
	}

	private JException syntaxError(String message, Throwable causedBy) {
		return new JException(getRow(), getColumn(), message, causedBy);
	}

	private int getRow() {
		return reader.getRow();
	}

	private int getColumn() {
		return reader.getColumn();
	}

}
