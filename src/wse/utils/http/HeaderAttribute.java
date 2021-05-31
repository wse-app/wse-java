package wse.utils.http;

import java.io.IOException;
import java.io.OutputStream;

import wse.utils.writable.StreamWriter;

public class HeaderAttribute implements StreamWriter {
	public String value;
	public String name;

	public HeaderAttribute(String row)
	{
		String[] parts = row.split(":", 2);
		
		if (parts.length >= 2)
		{
			this.name = parts[0].trim();
			this.value = parts[1].trim();
			
			if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\""))
				value = value.substring(1, value.length() - 1);
		}else
			this.name = row;
	}

	public HeaderAttribute(String name, String value) {
		this.name = name.trim();
		this.value = value.trim();
	}

	public boolean hasValue() {
		return value != null;
	}

	public String toString() {
		return new String(toByteArray());
	}

	public byte[] toByteArray() {
		byte[] b = new byte[length()];
		write(b, 0);
		return b;
	}

	/**
	 * Contains newline
	 * 
	 * @return
	 */
	public int length() {
		return name.length() + (value != null ? (2 + value.length() + (value.split("\n").length - 1)) : 0) + 2; // \r\n

	}

	public void writeToStream(OutputStream stream) throws IOException {
		stream.write(this.toByteArray());
	}

	protected int write(byte[] data, int off) {
		int p = off;
		System.arraycopy(name.getBytes(), 0, data, p, name.length());
		p += name.length();
		if (value != null) {
			data[p++] = ':';
			data[p++] = ' ';
			String[] parts = value.split("\n");

			for (int i = 0; i < parts.length; i++) {
				System.arraycopy(parts[i].getBytes(), 0, data, p, parts[i].length());
				p += parts[i].length();
				if (i < parts.length - 1) {
					data[p++] = '\n';
					data[p++] = ' ';
				}
			}
		}
		data[p++] = '\r';
		data[p++] = '\n';
		return p - off;
	}
}
