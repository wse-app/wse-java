package wse.utils.formdata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.utils.ArrayUtils;
import wse.utils.http.HttpAttributeList;
import wse.utils.http.StreamUtils;
import wse.utils.stream.CombinedInputStream;

public class FormData {

	public static final Logger log = WSE.getLogger();

	public static class Input {
		public Input(byte[] value) {
			byte[][] parts = ArrayUtils.split(value, "\r\n\r\n".getBytes(), 2, true);
			try {
				this.header = new HttpAttributeList(new String(parts[0], "UTF-8").trim().split("\r\n"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (parts.length > 1)
				this.data = parts[1];
			else
				this.data = new byte[0];
		}

		public HttpAttributeList header;
		public byte[] data;
	}

	public static List<Input> parseInput(InputStream input, String boundary) throws IOException {
		log.fine("FormData: Reading all");
		byte[] data = StreamUtils.readAll(new CombinedInputStream(new ByteArrayInputStream("\r\n".getBytes()), input));
		log.fine("FormData: Splitting on boundary");
		byte[][] split = ArrayUtils.split(data, ("\r\n--" + boundary).getBytes(), -1, true);
		log.fine("FormData: Got " + (split.length - 1) + " parts");

		List<Input> result = new LinkedList<>();
		for (byte[] b : split) {
			if (b[0] == b[1] && b[1] == '-') {
				log.finest("FormData: Got end");
				break;
			}
			if (log.isLoggable(Level.FINEST)) {
				log.finest("FormData: parsing part, length: " + b.length);
				log.finest("FormData: part: \n"
						+ (new String(b, 0, Math.min(512, b.length)) + (b.length > 512 ? "[...]" : "")));
			}
			result.add(new Input(b));
		}
		return result;
	}

	public static int indexOf(byte[] data, int dataOffset, int dataLength, byte[] part) {
		int eff = part.length / 5;
		if (eff < 2)
			eff = 2;
		for (int i = dataOffset; i < dataOffset + dataLength - part.length + 1; ++i) {
			boolean found = true;
			for (int j = 0; j < part.length; j += eff) {
				if (data[i + j] != part[j]) {
					found = false;
					break;
				}
			}
			if (!found)
				continue;
			for (int j = 0; j < part.length; ++j) {
				if (data[i + j] != part[j]) {
					found = false;
					break;
				}
			}
			if (found)
				return i;
		}
		return -1;
	}
}
