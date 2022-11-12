package wse.utils.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import wse.utils.exception.WebSocketException;

public class Frame implements WebSocketCodes {

    protected boolean fin;
    protected boolean masked;
    protected int opcode;
    protected int reserved;
    protected byte[] key;

    protected long payload_length;
    protected byte[] payload;

    public boolean isFin() {
        return fin;
    }

    public boolean isMasked() {
        return masked;
    }

    public int getOpcode() {
        return opcode;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getPayload() {
        return payload;
    }

    public boolean getRSV1() {
        return ((reserved & 0b100) >> 2) == 1;
    }

    public boolean getRSV2() {
        return ((reserved & 0b010) >> 1) == 1;
    }

    public boolean getRSV3() {
        return ((reserved & 0b001)) == 1;
    }

    public static Frame readNext(InputStream stream, boolean client) throws IOException {

        byte[] f2 = read(stream, 2);

        if (f2 == null) {
            throw new WebSocketException("got end of stream");
        }

        int first = f2[0];
        Frame result = new Frame();

        result.fin = ((first & FIN_MASK) >> 7) == 1; // bit 0 (1)
        result.reserved = ((first & RSV_MASK) >> 4) & 0b111; // bit 1-3 (3)
        result.opcode = first & OPCODE_MASK; // bit 4-7 (4)

        int second = f2[1];
        result.masked = ((second >> 7) & 0b1) == 1; // bit 8 (1)

        if (!client && !result.masked) {
            // Should abort socket: http://tools.ietf.org/html/rfc6455#section-5.1
            throw new WebSocketException("Server got unmasked websocket message");
        }

        // Read payload length
        int len_info = (second & LENGTH_MASK); // bit 9-15 (7)
        if (len_info <= 125) {
            result.payload_length = len_info;
        } else if (len_info == 126) {
            byte[] len = read(stream, 2);
            result.payload_length = ByteBuffer.wrap(len).getShort();
        } else { // len == 127
            byte[] len = read(stream, 8);
            result.payload_length = ByteBuffer.wrap(len).getLong();
        }
        // Read key if the payload is XOR masked
        if (result.masked) {
            result.key = read(stream, 4); // 32-bit key
        }

        result.payload = read(stream, (int) result.payload_length);
        return result;
    }

    /**
     * Reads a specified number of bytes, will return null if end of stream is
     * reached before every byte has been read.<br>
     * Blocks until <b>length</b> number of bytes has been read or end of stream is
     * reached.
     */
    private static byte[] read(InputStream stream, int length) throws IOException {
        byte[] result = new byte[length];
        if (length == 0) return result;
        int a = 0, p = 0;
        while ((a = stream.read(result, p, length - p)) != -1) {
            p += a;
            if (p >= result.length)
                return result;
        }
        return null;
    }
}