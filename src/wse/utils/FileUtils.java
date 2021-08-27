package wse.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import wse.utils.http.StreamUtils;

public class FileUtils {
	public static void forEachParentUntil(File first, File until, Consumer<File> consumer) {
		if (first == null || consumer == null)
			return;
		consumer.consume(first);
		while (first != null && !Objects.equals(first, until)) {
			first = first.getParentFile();
			if (first != null)
				consumer.consume(first);
		}
	}

	public static void forEachFileInTree(File parent, Consumer<File> consumer) {
		if (parent == null)
			return;
		consumer.consume(parent);

		if (parent.isDirectory()) {
			for (File f : parent.listFiles())
				forEachFileInTree(f, consumer);
		}
	}

	public static void write(File target, boolean append, byte[] source) throws IOException {
		write(target, append, new ByteArrayInputStream(source), source.length);
	}

	public static void write(File target, boolean append, InputStream source, int buffsize) throws IOException {
		FileOutputStream output = new FileOutputStream(target, append);
		StreamUtils.write(source, output, buffsize);
		output.close();
	}

	public static File getAbsolute(File root, String relative) {
		File tmp = new File(relative);
		if (tmp.isAbsolute())
			return tmp;

		return new File(root, relative);
	}
}
