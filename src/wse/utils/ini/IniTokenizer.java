package wse.utils.ini;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.logging.Logger;

import wse.WSE;
import wse.utils.ini.IniOptions.DuplicatePropertyRule;
import wse.utils.ini.IniOptions.WhitespaceRule;
import wse.utils.internal.HasRowColumn;
import wse.utils.internal.PushableReader;
import wse.utils.options.IOptions;
import wse.utils.options.Options;

public class IniTokenizer implements HasRowColumn {

	private static final Logger log = WSE.getLogger(IniTokenizer.class);

	private final PushableReader reader;

	private final DuplicatePropertyRule duplicate_rule;
	private final WhitespaceRule whitespace_rule;
	private final char key_value_separator;

	public static IniFile parse(InputStream input, Charset cs) throws IOException {
		return parse(input, cs, null);
	}

	public static IniFile parse(InputStream input, Charset cs, IOptions options) throws IOException {
		if (input == null)
			throw new NullPointerException("input == null");
		if (cs == null)
			throw new NullPointerException("cs == null");
		IniTokenizer tokenizer = new IniTokenizer(input, cs, options);
		return tokenizer.beginIni();
	}

	public IniTokenizer(InputStream input, Charset cs, IOptions options) {
		this(new InputStreamReader(input, cs), options);
	}

	public IniTokenizer(Reader reader, IOptions options) {
		if (reader == null)
			throw new NullPointerException("reader == null");
		this.reader = new PushableReader(reader);

		if (options == null)
			options = Options.EMPTY;

		duplicate_rule = options.get(IniOptions.DUPLICATE_PROPERTY_RULE);
		whitespace_rule = options.get(IniOptions.WHITESPACE_RULE);
		key_value_separator = options.get(IniOptions.KEY_VALUE_SEPARATOR);

	}

	public IniFile beginIni() throws IOException {

		IniFile file = new IniFile();
		IniSection section = null;

		while (true) {
			char c = nextSkipWS();
			switch (c) {
			case '[':
				int row = reader.getRow();
				int column = reader.getColumn();
				String sectionName = beginString(']');
				section = file.addSection(sectionName);
				section.setRow(row);
				section.setColumn(column);
				break;
			case ';':
			case '#':
				beginString((char) 0);
				break;
			default:
				reader.push(c);
				beginLine(section != null ? section : file);
			}

			if (reader.end())
				break;
		}

		return file;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void beginLine(IniSection section) throws IOException {

		String key = beginString(key_value_separator);
		if (key == null)
			return;
		
		String value = beginString((char) 0);

		switch (whitespace_rule) {
		case IGNORE:
			break;
		case REMOVE:
			key = key.replaceAll("\\s", "");
			break;
		case TRIM:
			key = key.trim();
			value = value.trim();
			break;
		}

		Object existing = section.get(key);
//		System.out.println(existing);
		if (existing == null) {
			section.setValue(key, value);
			return;
		}

		switch (duplicate_rule) {
		case APPEND:
			value = String.valueOf(existing) + value;
			section.setValue(key, value);
			break;
		case ARRAY:
			if (existing instanceof LinkedList) {
				((LinkedList) existing).add(value);
			} else {
				LinkedList<String> list = new LinkedList<>();
				list.add(String.valueOf(existing));
				list.add(value);
				section.setValue(key, list);
			}
			break;
		case IGNORE:
			break;
		case OVERRIDE:
			section.setValue(key, value);
			break;
		}

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
				if (sb.length() == 0) return null;
				
				if (end == 0)
					return sb.toString();
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
				default: {

					log.warning("Escape character not recognized: '" + c + "'");
					sb.append(c);
					break;

				}
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

	private IniException syntaxError(String message) {
		return syntaxError(message, null);
	}

	private IniException syntaxError(String message, Throwable causedBy) {
		return new IniException(getRow(), getColumn(), message, causedBy);
	}

	@Override
	public int getRow() {
		return reader.getRow();
	}

	@Override
	public int getColumn() {
		return reader.getColumn();
	}

}
