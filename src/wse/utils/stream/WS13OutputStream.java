package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Level;

import wse.WSE;
import wse.utils.SerializationWriter;
import wse.utils.exception.WseException;
import wse.utils.websocket.WebSocketCodes;

public class WS13OutputStream extends BufferedOutputStream implements WebSocketCodes {
	Random random = new Random();

	public WS13OutputStream(OutputStream writeTo, boolean masked) {
		super(new RecordingOutputStream(writeTo, WSE.getLogger(), Level.FINEST, "WS13 out"), 8192, 14, 0);
//		super(writeTo, 5, 14, 0);
		forceBufferSize(true);
		this.masked = masked;
	}

	private int frameCount = 0;
	private boolean isLastFrame = false;

	private boolean masked;
	private byte opcode = OP_BINARY;

	private byte[] bufferData;
	private Content buffFrame;

	private boolean useRandomKey = false;

	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	public void setUseRandomMaskKey(boolean useRandomKey) {
		this.useRandomKey = useRandomKey;
	}

	private byte[] randomKey() {
		byte[] res = new byte[4];
		if (useRandomKey)
			random.nextBytes(res);
		return res;
	}

	private int opcode() {
		if (frameCount != 0)
			return WebSocketCodes.OP_CONTINUE;
		return opcode;
	}

	public void setOpCode(int opcode) {
		this.opcode = (byte) opcode;
	}

	public boolean isReadyForNextMessage() {
		return length() == 0 && frameCount == 0 && buffFrame == null;
	}

	@Override
	protected void writeBuffer(byte[] data, int offset, int length) throws IOException {
		if (buffFrame != null) {
			sendFrame(buffFrame, false);
			buffFrame = null;
		}

		if (isLastFrame) {
			// from flush
			sendFrame(new Content(data, offset, length), true);
		} else {
			if (bufferData == null) {
				bufferData = new byte[data.length];
			}
			System.arraycopy(data, offset, bufferData, offset, length);
			buffFrame = new Content(bufferData, offset, length);
		}

	}

	private void sendFrame(Content content, boolean lastFrame) throws IOException {
		byte[] data = content.data;
		int offset = content.off;
		int length = content.len;

		if (offset < 14)
			throw new WseException("Output buffer needs at least 14 bytes prefix");

		byte payLen = (byte) ((length > 0xffff) ? 127 : ((length > 125) ? 126 : ((length >= 0) ? length : 0)));

		int header_length = (2 + (masked ? 4 : 0) + ((payLen == 127) ? 8 : ((payLen == 126) ? 2 : 0)));
		int hoff = offset - header_length;

		if (masked) {
			byte[] key = randomKey();
			xor(data, offset, length, key);
			data[offset - 4] = key[0];
			data[offset - 3] = key[1];
			data[offset - 2] = key[2];
			data[offset - 1] = key[3];
		}

		data[hoff++] = (byte) (opcode() | (lastFrame ? (1 << 7) : 0));
		data[hoff++] = (byte) (payLen | (masked ? (1 << 7) : 0));

		if (payLen == 126) {
			SerializationWriter.writeBytes(data, hoff, (short) length);
		} else if (payLen == 127) {
			SerializationWriter.writeBytes(data, hoff, (long) length);
		}

		writeTo.write(data, offset - header_length, length + header_length);
		frameCount++;
	}

	public static void xor(byte[] data, int offset, int length, byte[] key) {
		if (isKeyIdentity(key))
			return;
		for (int i = 0; i < length; i++)
			data[offset + i] ^= key[i % key.length];
	}

	public static boolean isKeyIdentity(byte[] data) {
		for (int i = 0; i < data.length; i++)
			if (data[i] != 0)
				return false;
		return true;
	}

	private void resetMessage() {
		isLastFrame = false;
		frameCount = 0;
	}

	@Override
	public void flush() throws IOException {
		isLastFrame = true;
		finish();
		writeTo.flush();
		resetMessage();
	}

	static class Content {
		public final byte[] data;
		public final int off, len;

		public Content(byte[] data, int off, int len) {
			this.data = data;
			this.off = off;
			this.len = len;
		}
	}
}
