package wse.utils.saveable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SaveableFile {
	private File file;

	public SaveableFile() {
	}

	public SaveableFile(File file) {
		this.file = file;
	}

	public void save() {
		if (file == null) {
			return;
		}
		save(file);
	}

	public void save(File file) {
		try {
			save(file, this.toString(), true);
			setFilePath(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFilePath(File file) {
		this.file = file;
	}

	public String toString() {
		return "saveable.SaveableFile.toString(): Default File Text!\nPlease override .toString()";
	}

	public static boolean save(File file, String data, boolean override) throws IOException {
		return save(file, data.getBytes(), override);
	}

	public static boolean save(File file, byte[] data, boolean override) throws IOException {
		return save(file, new ByteArrayInputStream(data), override);
	}

	public static boolean save(File file, InputStream source, boolean override) throws IOException {
		if (file == null)
			return false;
		if (!file.exists()) {
			if (file.getParentFile() == null)
				return false;
			file.getParentFile().mkdirs();
			file.createNewFile();
			if (!file.canWrite()) {
				throw new IOException("Failed to create a writeable file!");
			}
		} else {
			if (!override)
				return false;
		}

		if (!file.canWrite()) {
			if (!file.setWritable(true)) {
				throw new IOException("Failed to save file, file can not be written to!");
			}
		}

		try (FileOutputStream writer = new FileOutputStream(file, false)) {
			byte[] buff = new byte[8192];
			int a = 0;
			while ((a = source.read(buff)) > 0) {
				writer.write(buff, 0, a);
			}
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
