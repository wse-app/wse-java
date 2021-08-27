package wse.utils.websocket;

/**
 * <br>
 * WebSocket frame structure:
 * 
 * 
 * 
 * <pre>
 * <code>
 0               1               2               3
 0 1 2 3 4 5 6 7 8 9 a b c d e f 0 1 2 3 4 5 6 7 8 9 0 a b c d e
+-+-+-+-+-------+-+-------------+-------------------------------+
|F|R|R|R| opcode|M| Payload len |    Extended payload length    |
|I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
|N|V|V|V|       |S|             |   (if payload len==126/127)   |
| |1|2|3|       |K|             |                               |
+-+-+-+-+-------+-+-------------+       -       -       -       +
|     Extended payload length continued, if payload len == 127  |
+       -       -       -       +-------------------------------+
|                               |Masking-key, if MASK set to 1  |
+-------------------------------+-------------------------------+
| Masking-key (continued)       |          Payload Data         |
+--------------------------------       -       -       -       +
:                     Payload Data continued ...                :
+       -       -       -       -       -       -       -       +
|                     Payload Data continued ...                |
+---------------------------------------------------------------+
</code>
 * </pre>
 * 
 * 
 * 
 * 
 * 
 * first 2 bytes is FIN (1), RESERVED (3), OPCODE (4), MASK (1), PAYLOAD INFO
 * (7)<br>
 * <br>
 * 
 * 
 * OPCode indicates the type of content: <br>
 * - 0 (CONTINUE) means an addition to previously sent frame payload<br>
 * - 1 (TEXT) means text content (always UTF-8)<br>
 * - 2 (BINARY) means octet-stream content<br>
 * <br>
 * 
 * FIN=0 indicates that the payload of the next frame(s) should be concatenated
 * with the payload of this frame <br>
 * FIN=1 indicates message payload/end of message (if the previous frame was
 * FIN=0)<br>
 * All frames other than the first in a message must have the OPCode set to 0
 * (CONTINUE)<br>
 * The last frame of a message must have FIN=1<br>
 * <br>
 * 
 * Content is masked using XOR encryption with the 32-bit KEY only if
 * MASKED=1<br>
 * Key should be present only if MASKED=1<br>
 * <br>
 * 
 */
public interface WebSocketCodes {
	public static final byte OP_CONTINUE = 0x0;
	public static final byte OP_TEXT = 0x1;
	public static final byte OP_BINARY = 0x2;

	public static final byte OP_CLOSE = 0x8;
	public static final byte OP_PING = 0x9;
	public static final byte OP_PONG = 0xA;

	public static final int LENGTH_MASK = 0b01111111;
	public static final int MASKED_MASK = 0b10000000;

	public static final int FIN_MASK = 0b10000000;
	public static final int RSV_MASK = 0b01110000;
	public static final int OPCODE_MASK = 0b00001111;

	public static final byte FIN = (byte) 0b10000000;
	public static final byte CONTINUE = 0;

	public static final byte MASKED = (byte) 0b10000000;

}
