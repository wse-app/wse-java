package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Level;

import wse.WSE;
import wse.utils.SerializationWriter;
import wse.utils.exception.WseException;
import wse.utils.websocket.WebSocket;
import wse.utils.websocket.WebSocketCodes;

public class WS13OutputStream extends BufferedOutputStream implements WebSocketCodes {
	
	public static boolean LOG_HEX = true;
	
	Random random = new Random();

	public WS13OutputStream(OutputStream writeTo, boolean masked) {
		super(new RecordingOutputStream(writeTo, WSE.getLogger(WebSocket.LOG_CHILD_NAME), Level.FINEST, "WS13 OUT", LOG_HEX), 8192, 14, 0);
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
		int contentLength = content.len;

		boolean hasMask = masked && contentLength > 0;
		
		if (offset < 14)
			throw new WseException("Output buffer needs at least 14 bytes prefix");

		byte lengthByte = (byte) ((contentLength > 0xffff) ? 127 : ((contentLength > 125) ? 126 : ((contentLength >= 0) ? contentLength : 0)));

		int headerLength = 2 + (hasMask ? 4 : 0) + ((lengthByte == 127) ? 8 : ((lengthByte == 126) ? 2 : 0));
		int headerOffset = offset - headerLength;

		if (hasMask) {
			byte[] key = randomKey();
			xor(data, offset, contentLength, key);
			data[offset - 4] = key[0];
			data[offset - 3] = key[1];
			data[offset - 2] = key[2];
			data[offset - 1] = key[3];
		}

		data[headerOffset++] = (byte) (opcode() | (lastFrame ? (1 << 7) : 0));
		data[headerOffset++] = (byte) (lengthByte | (hasMask ? (1 << 7) : 0));

		if (lengthByte == 126) {
			SerializationWriter.writeBytes(data, headerOffset, (short) contentLength);
		} else if (lengthByte == 127) {
			SerializationWriter.writeBytes(data, headerOffset, (long) contentLength);
		}

		writeTo.write(data, offset - headerLength, contentLength + headerLength);
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
