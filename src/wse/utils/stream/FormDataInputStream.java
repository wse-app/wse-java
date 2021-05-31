package wse.utils.stream;

import java.io.InputStream;

public class FormDataInputStream extends SuffixedInputStream {

	public FormDataInputStream(InputStream readFrom, String boundary) {
		super(readFrom, 8192, ("\r\n--" + boundary).getBytes());
		
		this.data[0][0] = '\r';
		this.data[0][1] = '\n';
//		this.data[0][2] = '-';
//		this.data[0][3] = '-';
		this.available = 2;
	}

}
