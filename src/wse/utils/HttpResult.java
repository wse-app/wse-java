package wse.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.utils.exception.WseException;
import wse.utils.http.HttpHeader;
import wse.utils.http.TransferEncoding;
import wse.utils.stream.ChunkedInputStream;
import wse.utils.stream.LimitedInputStream;
import wse.utils.stream.RecordingInputStream;

public class HttpResult {
	
	private final HttpHeader header;
	
	private final InputStream content_raw;
	private InputStream content;

	public HttpResult(HttpHeader header, InputStream content) {
		this(header, content, true);
	}
	
	public HttpResult(HttpHeader header, InputStream content, boolean modifyContent) {
		super();
		this.header = header;
		this.content_raw = content;
		
		if (modifyContent) {
			TransferEncoding enc = header.getTransferEncoding();
			if (enc == null)
				enc = TransferEncoding.IDENTITY;
			
			switch(enc) {
			case BR:
			case COMPRESS:
			case DEFLATE:
			case GZIP:
				throw new WseException("Transfer-Encoding " + enc.name + " not supported");
			case CHUNKED:
				this.content = new ChunkedInputStream(content);
				break;
			case IDENTITY:
				long cl = header.getContentLength();
				if (cl >= 0) {
					this.content = new LimitedInputStream(content, (int) cl);				
					break;
				}
				// fall through
			default:
				this.content = content;
				break;
			}
		}else {
			this.content = content;
		}
		
	}
	
	protected void wrapLogger(String title, Logger logger, Level level) {
		this.content = new RecordingInputStream(this.content, logger, level, 4096, title);
	}

	public HttpHeader getHeader() {
		return header;
	}

	public InputStream getContent() {
		return content;
	}
	
	public InputStream getContentRaw() {
		return content_raw;
	}
	
	public void closeInput() {
		if (content != null) {
			try {
				content.close();
			} catch (IOException e) {
			}
		}
	}
}
