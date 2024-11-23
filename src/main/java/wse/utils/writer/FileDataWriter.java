package wse.utils.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import wse.utils.MimeType;
import wse.utils.http.HttpHeader;
import wse.utils.http.StreamUtils;
import wse.utils.http.TransferEncoding;

public class FileDataWriter implements HttpWriter {

	private final File file;
	private MimeType mt;
	private Charset cs;

	public FileDataWriter(File file) throws FileNotFoundException {
		this.file = file;
		mt = MimeType.getByExtension(file);
	}

	public void setMimeType(MimeType mt) {
		this.mt = mt;
	}

	public void setCharset(Charset cs) {
		this.cs = cs;
	}

	@Override
	public long requestContentLength(Charset cs) {
		return file.length();
	}

	@Override
	public void writeToStream(OutputStream output, Charset charset) throws IOException {
		long length = file.length();
		try (InputStream is = new FileInputStream(file)) {
			StreamUtils.write(is, output, (int) Math.min(20000, length));
		}
	}

	@Override
	public void prepareHeader(HttpHeader header) {
		if (file.length() < 0)
			header.setTransferEncoding(TransferEncoding.CHUNKED);

		if (mt != null) {
			header.setContentType(mt.withCharset(cs));
		}
	}

}
