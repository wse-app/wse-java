package wse.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import wse.WSE;
import wse.utils.HttpCodes;
import wse.utils.MimeType;
import wse.utils.http.ContentDisposition;
import wse.utils.http.ErrorFormatter;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.http.HttpStatusLine;
import wse.utils.stream.IdentityOutputStream;
import wse.utils.stream.WseOutputStream;

public final class HttpServletResponse extends WseOutputStream {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private static final Logger log = WSE.getLogger();
	private final HttpHeader header;
	private final HttpStatusLine statusLine;
	
	private boolean force_enable_output = false;

	protected String err_msg;
	
	private ErrorFormatter errorFormatter;

	public HttpServletResponse(OutputStream output) {
		this(output instanceof WseOutputStream ? ((WseOutputStream) output) : new IdentityOutputStream(output));
	}

	public HttpServletResponse(WseOutputStream output) {
		super(output);
		if (output == null)
			throw new IllegalArgumentException("OutputStream is null");

		this.header = new HttpHeader(statusLine = HttpStatusLine.fromCode(200));

		header.setAttribute("Server", "WebServiceEngine/" + WSE.VERSION);
	}

	private boolean headerWritten = false;

	public void writeHeader() throws IOException {
		if (headerWritten)
			throw new IllegalStateException("Can't write header twice");
		
		
		if (header.getAttribute("Connection") == null)
			header.setAttribute("Connection", "close");

		HttpStatusLine sl = this.header.getStatusLine();
		if (sl != null) {
			log.fine("Responding: " + sl.getStatusCode() + " " + sl.getStatusMessage() + (this.err_msg != null ? (" err: " + this.err_msg) : ""));
		}

		byte[] h = header.toByteArray();
		log.finest("Response Header: [" + h.length + " bytes]\n" + new String(h));

		if (!force_enable_output) {
			if (!MimeType.isText(header.getContentType().mimeType) && !header.getContentType().is(MimeType.application.xml)) {
				disableOutputLogging();
			}
		}
		
		// order important
		headerWritten = true;
		header.writeToStream(this, UTF8);
	}

	public void setStatusCode(int code) {
		if (headerWritten)
			throw new IllegalStateException("Can't set status code after header has been written");
		statusLine.setStatusCode(code);
	}

	public void sendError(int code) throws IOException {
		sendError(code, (String) null);
	}
	
	public ErrorFormatter getErrorFormatter() {
		if (this.errorFormatter != null)
			return this.errorFormatter;
		return ErrorFormatter.DEFAULT;
	}
	
	public void setErrorFormatter(ErrorFormatter formatter) {
		this.errorFormatter = formatter;
	}

	public void sendError(int code, String message) throws IOException {
		if (headerWritten)
			throw new IllegalStateException("Can't write header twice");
		statusLine.setStatusCode(code);
		this.err_msg = message;
		
		byte[] data = getErrorFormatter().error(code, message, getHttpHeader());
		if (data != null) {
			setContentLength(data.length);			
			writeHeader();
			write(data);
		}else {
			writeHeader();
		}
	}
	
	public void sendError(int code, Throwable cause) throws IOException {
		if (headerWritten)
			throw new IllegalStateException("Can't write header twice");
		statusLine.setStatusCode(code);
		this.err_msg = cause.getMessage();
		
		byte[] data = getErrorFormatter().error(code, cause, getHttpHeader());
		if (data != null) {
			setContentLength(data.length);			
			writeHeader();
			write(data);
		}else {
			writeHeader();
		}
	}

	public void sendMethodNotAllowed(HttpMethod... allowedMethods) throws IOException {
		if (headerWritten)
			throw new IllegalStateException("Can't write header twice");
		header.setAllow(allowedMethods);
		sendError(HttpCodes.METHOD_NOT_ALLOWED);
	}

	public void setContentLength(long contentLength) {
		if (headerWritten)
			throw new IllegalStateException("Can't set content length after header has been written");

		header.setSendContentLength(contentLength >= 0);
		header.setContentLength(contentLength);
	}

	public void setContentType(MimeType type) {
		if (type != null) {
			setContentType(type.toString());
		} else {
			removeAttribute("Content-Type");
		}
	}

	public void setContentDisposition(ContentDisposition disposition) {
		if (disposition != null) {
			setContentDisposition(disposition.toString());
		} else {
			removeAttribute("Content-Disposition");
		}
	}

	public void setContentDisposition(String disposition) {
		setAttribute("Content-Disposition", disposition);
	}

	public void setContentType(MimeType type, String charset) {
		setContentType(type.withCharset(charset));
	}

	public void setContentType(String content_type) {
		setAttribute("Content-Type", content_type);
	}

	/**
	 * Use the http header only for setting attributes, do not call
	 * HttpHeader.writeToStream(HttpServletResponse), this will be taken care of
	 * automatically at first HttpServletResponse.write().
	 */
	public HttpHeader getHttpHeader() {
		return header;
	}

	public void removeAttribute(String attributeKey) {
		header.removeAttribute(attributeKey);
	}

	public void setAttribute(String key, String value) {
		if (headerWritten)
			throw new IllegalStateException("Can't update header after it has been written");
		if (value == null) {
			removeAttribute(key);
			return;
		}
		header.setAttribute(key, value);
	}

	public void setAllow(String allow) {
		header.setAllow(allow);
	}

	@Override
	public void write(int b) throws IOException {
		if (!headerWritten)
			writeHeader();
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (!headerWritten)
			writeHeader();
		super.write(b, off, len);

	}

	
	@Override
	public void disableOutputLogging() {
		super.disableOutputLogging();
	}
	
	public void forceEnableMessageLogging() {
		this.force_enable_output = true;
	}
}
