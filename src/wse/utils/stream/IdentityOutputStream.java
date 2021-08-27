package wse.utils.stream;

import java.io.OutputStream;

public class IdentityOutputStream extends WseOutputStream {

	public IdentityOutputStream(OutputStream original) {
		super(original);
	}
}
