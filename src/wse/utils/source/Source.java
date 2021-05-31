package wse.utils.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import wse.utils.LinkedByteArray;
import wse.utils.http.StreamUtils;

public final class Source {

	private Source() {
	}

	public final static byte[] read(File file) throws IOException {
		return StreamUtils.readAll(new FileInputStream(file));
	}
	
	public final static String getContainingText(File file) {
		LinkedByteArray lba = toByteArray(file);
		if (lba == null)
			return null;
		return lba.toString();
	}

	public final static String getContainingText(InputStream file) {
		LinkedByteArray lba = toByteArray(file);
		if (lba == null)
			return null;
		return lba.toString();
	}

	public final static LinkedByteArray toByteArray(File file) {
		try (InputStream stream = new FileInputStream(file)) {
			return toByteArray(stream);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public final static LinkedByteArray toByteArray(InputStream file) {
		LinkedByteArray lba = new LinkedByteArray();

		byte[] buff = new byte[5000];
		int actual;
		int ptr = 0;

		try {
			while (true) {
				try {
					actual = file.read(buff, ptr, buff.length - ptr);

					if (actual == -1)
						break;

					lba.append(buff, 0, actual - 1);
					ptr += actual;

					if (ptr > buff.length / 2) {
						buff = new byte[buff.length];
						ptr = 0;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		} finally {
			try {
				file.close();
			} catch (IOException e) {
			}
		}

		return lba;
	}

	public final static InputStream getResource(Class<?> clazz, String relative_to_jar) {
		return clazz.getResourceAsStream(relative_to_jar);
	}
}
