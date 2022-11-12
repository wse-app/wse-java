package wse.utils.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import wse.utils.exception.WebSocketException;
import wse.utils.stream.CombinedInputStream;
import wse.utils.stream.WS13OutputStream;
import wse.utils.stream.XORInputStream;

public class Message {
	private final List<Frame> frames = new LinkedList<>();

	public List<Frame> getFrames() {
		return frames;
	}

	public int getOPCode() {
		if (frames.size() <= 0)
			return -1;

		return frames.get(0).getOpcode();
	}

	public long getContentLength() {
		long res = 0;
		for (Frame f : frames)
			res += f.payload_length;
		return res;
	}

	public boolean isEveryFrameMasked() {
		for (Frame f : frames)
			if (!f.isMasked())
				return false;
		return true;
	}

	public InputStream inputStream() {
		InputStream[] streams = new InputStream[frames.size()];
		for (int i = 0; i < frames.size(); i++) {
			Frame f = frames.get(i);
			streams[i] = new ByteArrayInputStream(f.getPayload());
			if (f.isMasked() && !WS13OutputStream.isKeyIdentity(f.getKey())) {
				streams[i] = new XORInputStream(streams[i], f.getKey());
			}
		}
		return new CombinedInputStream(streams);
	}

	public static Message readNext(InputStream stream, boolean client) throws IOException {
		Message message = new Message();

		while (true) {
			try {
				Frame f = Frame.readNext(stream, client);
				message.frames.add(f);

				if (f.isFin()) {
					return message;
				}
			} catch (Exception e) {
				throw new WebSocketException("Could not read next frame: " + e.getMessage(), e);
			}

		}
	}
}
