package wse.utils.json;

import java.io.IOException;
import java.io.OutputStream;

import wse.utils.writable.StreamWriter;

public abstract class JSONValue implements StreamWriter {
	@SuppressWarnings("unchecked")
	public <T> T cast() {
		return (T) this;
	}
	
	public abstract String toString(int level);
	
	@Override
	public void writeToStream(OutputStream stream) throws IOException {
		stream.write(this.toString().getBytes());
	}
}
