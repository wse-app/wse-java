package wse.utils.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import wse.utils.internal.PrettyPrinter;
import wse.utils.internal.StringGatherer;
import wse.utils.writable.StreamWriter;

/**
 * Two subclassed: HttpStatusLine and HttpRequestLine
 */
public abstract class HttpDescriptionLine implements StreamWriter, PrettyPrinter {

	private static final Pattern ResponsePattern = Pattern.compile("(Secure-)?HTTP/1.([0-2]|4) [1-9][0-9][0-9]( .*)?",
			Pattern.DOTALL);
//	private static final Pattern RequestPattern = Pattern.compile("(" + HttpMethod.toPattern("|") + ") [^ ]* (Secure-)?HTTP/1.([0-2]|4)");

	public static final String HTTP11 = "HTTP/1.1";

	protected String httpVersion;

	public HttpDescriptionLine() {
		this.httpVersion = HTTP11;
	}

	public HttpDescriptionLine(String httpVersion) {
		if (httpVersion == null)
			throw new NullPointerException();
		this.httpVersion = httpVersion;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	/**
	 * Should not contain newline
	 * 
	 * @return
	 */
	public abstract int length();

	public abstract int write(byte[] dest, int off);

	@Deprecated
	public abstract byte[] toByteArray();

	public static HttpDescriptionLine fromString(String line) {
		if (line == null)
			return null;

		if (ResponsePattern.matcher(line).find()) {
			return HttpStatusLine.fromString(line);
		} else {
			return HttpRequestLine.fromString(line);
		}
	}

	public static HttpDescriptionLine copy(HttpDescriptionLine line) {
		if (line instanceof HttpRequestLine) {
			return new HttpRequestLine((HttpRequestLine) line);
		} else if (line instanceof HttpStatusLine) {
			return new HttpStatusLine((HttpStatusLine) line);
		}
		return null;
	}

	public boolean isRequest() {
		return (this instanceof HttpRequestLine);
	}

	public boolean isResponse() {
		return (this instanceof HttpStatusLine);
	}

	public StringGatherer prettyPrint() {
		return prettyPrint(0);
	}

	public StringGatherer prettyPrint(int level) {
		StringGatherer builder = new StringGatherer();
		prettyPrint(builder, level);
		return builder;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		prettyPrint().writeToStream(stream, charset);
	}

	@SuppressWarnings("unchecked")
	public <T extends HttpDescriptionLine> T cast() {
		return (T) this;
	}

}
