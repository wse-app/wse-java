package wse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import wse.utils.exception.WseException;
import wse.utils.http.HttpHeader;
import wse.utils.http.StreamUtils;
import wse.utils.http.TransferEncoding;

public class FileDataWriter extends HttpWriter{

	private InputStream stream;
	private long length;
	
	public FileDataWriter(File file) throws FileNotFoundException {
		this(new FileInputStream(file), file.length());
	}
	
	public FileDataWriter(InputStream input) {
		this(input, -1);
	}
	
	public FileDataWriter(InputStream input, long length) {
		if (input == null)
			throw new WseException("Got null input");
		this.stream = input;
		this.length = length;
	}
	
	@Override
	public long requestContentLength() {
		return length;
	}

	@Override
	public void writeToStream(OutputStream output) throws IOException {
		StreamUtils.write(stream, output, (int) (this.length == -1 ? 200000 : Math.min(200000, this.length)));
	}
	
	@Override
	public void prepareHeader(HttpHeader header) {
		if (this.length < 0)
			header.setTransferEncoding(TransferEncoding.CHUNKED);
	}

}
