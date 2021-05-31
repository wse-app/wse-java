package wse.utils;

import java.nio.ByteBuffer;

public class SerializationWriter
{

	public static int writeBytes(byte[] dest, int pointer, String value)
	{
		pointer = writeBytes(dest, pointer, (short) value.length());
		return writeBytes(dest, pointer, value.getBytes());
	}

	public static int writeBytes(byte[] dest, int pointer, byte[] src)
	{
		for (int i = 0; i < src.length; i++)
		{
			dest[pointer++] = src[i];
		}
		return pointer;
	}

	public static int writeBytes(byte[] dest, int pointer, byte value)
	{
		dest[pointer++] = value;
		return pointer;
	}

	public static int writeBytes(byte[] dest, int pointer, short value)
	{
		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer++] = (byte) ((value >> 0) & 0xff);
		return pointer;
	}

	public static int writeBytes(byte[] dest, int pointer, char value)
	{
		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer++] = (byte) ((value >> 0) & 0xff);
		return pointer;
	}

	public static int writeBytes(byte[] dest, int pointer, int value)
	{
		dest[pointer++] = (byte) ((value >> 24) & 0xff);
		dest[pointer++] = (byte) ((value >> 16) & 0xff);
		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer++] = (byte) ((value >> 0) & 0xff);
		return pointer;
	}

	public static int writeBytes(byte[] dest, int pointer, long value)
	{
		dest[pointer++] = (byte) ((value >> 56) & 0xff);
		dest[pointer++] = (byte) ((value >> 48) & 0xff);
		dest[pointer++] = (byte) ((value >> 40) & 0xff);
		dest[pointer++] = (byte) ((value >> 32) & 0xff);
		dest[pointer++] = (byte) ((value >> 24) & 0xff);
		dest[pointer++] = (byte) ((value >> 16) & 0xff);
		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer++] = (byte) ((value >> 0) & 0xff);
		return pointer;
	}

	public static int writeBytes(byte[] dest, int pointer, float value)
	{
		int data = Float.floatToIntBits(value);
		return writeBytes(dest, pointer, data);
	}

	public static int writeBytes(byte[] dest, int pointer, double value)
	{
		long data = Double.doubleToLongBits(value);
		return writeBytes(dest, pointer, data);
	}

	public static int writeBytes(byte[] dest, int pointer, boolean value)
	{
		dest[pointer++] = (byte) (value ? 1 : 0);
		return pointer;
	}

	public static byte[] getBytes(String value)
	{
		return value.getBytes();
	}

	public static byte[] getBytes(byte[] src)
	{
		return src;
	}

	public static byte[] getBytes(byte value)
	{
		return new byte[]
		{ value };
	}

	public static byte[] getBytes(short value)
	{
		byte[] dest = new byte[2];
		int pointer = 0;

		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer] = (byte) ((value >> 0) & 0xff);
		return dest;
	}

	public static byte[] getBytes(char value)
	{
		byte[] dest = new byte[2];
		int pointer = 0;

		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer] = (byte) ((value >> 0) & 0xff);
		return dest;
	}

	public static byte[] getBytes(int value)
	{
		byte[] dest = new byte[4];
		int pointer = 0;

		dest[pointer++] = (byte) ((value >> 24) & 0xff);
		dest[pointer++] = (byte) ((value >> 16) & 0xff);
		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer] = (byte) ((value >> 0) & 0xff);
		return dest;
	}

	public static byte[] getBytes(long value)
	{
		byte[] dest = new byte[8];
		int pointer = 0;

		dest[pointer++] = (byte) ((value >> 56) & 0xff);
		dest[pointer++] = (byte) ((value >> 48) & 0xff);
		dest[pointer++] = (byte) ((value >> 40) & 0xff);
		dest[pointer++] = (byte) ((value >> 32) & 0xff);
		dest[pointer++] = (byte) ((value >> 24) & 0xff);
		dest[pointer++] = (byte) ((value >> 16) & 0xff);
		dest[pointer++] = (byte) ((value >> 8) & 0xff);
		dest[pointer] = (byte) ((value >> 0) & 0xff);
		return dest;
	}

	public static byte[] getBytes(float value)
	{
		int data = Float.floatToIntBits(value);
		return getBytes(data);
	}

	public static byte[] getBytes(double value)
	{
		long data = Double.doubleToLongBits(value);
		return getBytes(data);
	}

	public static byte[] getBytes(boolean value)
	{
		return new byte[]
		{ (byte) (value ? 1 : 0) };
	}

	/*
	 * 
	 * Reading bytes from source byte array
	 */

	public static byte readByte(byte[] src, int pointer)
	{
		validate(src, pointer, 1, "byte");
		return (byte) ((src[pointer]));
	}

	public static short readShort(byte[] src, int pointer)
	{
		validate(src, pointer, 2, "short");
		return (short) ((src[pointer] << 8) | (src[pointer + 1]));
	}

	public static char readChar(byte[] src, int pointer)
	{
		validate(src, pointer, 2, "char");
		return (char) ((src[pointer] << 8) | (src[pointer + 1]));
	}

//	public static int readInt(byte[] src, int pointer)
//	{
//		validate(src, pointer, 4, "int");
//
//		return (int) (Byte.toUnsignedInt(src[pointer]) << 24)
//		        | (Byte.toUnsignedInt(src[pointer + 1]) << 16)
//		        | (Byte.toUnsignedInt(src[pointer + 2]) << 8)
//		        | (Byte.toUnsignedInt(src[pointer + 3]) << 0);
//	}

//	public static long readLong(byte[] src, int pointer)
//	{
//		validate(src, pointer, 8, "long");
//		return (long) ((Byte.toUnsignedInt(src[pointer]) << 56)
//		        | (Byte.toUnsignedInt(src[pointer + 1]) << 48)
//		        | (Byte.toUnsignedInt(src[pointer + 2]) << 40)
//		        | (Byte.toUnsignedInt(src[pointer + 3]) << 32)
//		        | (Byte.toUnsignedInt(src[pointer + 4]) << 24)
//		        | (Byte.toUnsignedInt(src[pointer + 5]) << 16)
//		        | (Byte.toUnsignedInt(src[pointer + 6]) << 8) | (Byte
//		            .toUnsignedInt(src[pointer + 7])));
//	}

//	public static float readFloat(byte[] src, int pointer)
//	{
//		validate(src, pointer, 4, "float");
//		return Float.intBitsToFloat(readInt(src, pointer));
//	}

	public static double readDouble(byte[] src, int pointer)
	{
		validate(src, pointer, 8, "double");
		
		return toDouble(readPart(src, pointer, 8));
	}
	
	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}

	public static boolean readBoolean(byte[] src, int pointer)
	{
		validate(src, pointer, 1, "boolean");
		assert (src[pointer] == 0 || src[pointer] == 1);
		return src[pointer] != 0;
	}

	private static void validate(byte[] src, int pointer, int min, String type)
	{
		if (pointer >= src.length)
			throw new IllegalArgumentException(
			        "Pointer can't be longer than source length: "
			                + (pointer + 1) + "/" + (src.length));
		if (src.length < min)
			throw new IllegalArgumentException(
			        "The specified byte[] source at " + pointer + "/" + src.length + " is not a " + type);
	}

	public static byte readByte(byte[] src)
	{
		return readByte(src, 0);
	}

	public static short readShort(byte[] src)
	{
		return readShort(src, 0);
	}

	public static char readChar(byte[] src)
	{
		return readChar(src, 0);
	}

//	public static int readInt(byte[] src)
//	{
//		return readInt(src, 0);
//	}
//
//	public static long readLong(byte[] src)
//	{
//		return readLong(src, 0);
//	}
//
//	public static float readFloat(byte[] src)
//	{
//		return readFloat(src, 0);
//	}

	public static double readDouble(byte[] src)
	{
		return readDouble(src, 0);
	}

	public static boolean readBoolean(byte[] src)
	{
		return readBoolean(src, 0);
	}

	public static String readString(byte[] src)
	{
		String result = "";

		for (int i = 0; i < src.length; i++)
		{
			result += (char) src[i];
		}
		return result;
	}

	/*
	 * 
	 * Other methods for binary stuff
	 */

	public static byte[] readPart(byte[] src, int start, int size)
	{
		if (size < 1)
			throw new IllegalArgumentException("Size can't be ZERO or negative");
		byte[] result = new byte[size];

		for (int i = 0; i < size; i++)
		{
			result[i] = src[start + i];
		}
		return result;
	}

	public static byte[] readBetween(byte[] src, int start, int end)
	{
		if (end < start)
			throw new IllegalArgumentException(
			        "Start can't be a greater value than end");
		byte[] result = new byte[end - start + 1];

		for (int i = 0; i <= end; i++)
		{
			result[i] = src[start + i];
		}
		return result;
	}
	
	public static byte[] getAfterOffset(byte[] src, int offset)
	{
		byte[] result = new byte[src.length - offset];
		for (int i = 0; i < src.length; i++)
		{
			if (i > offset)
			{
				int index = i - offset;
				result[index] = src[i];
			}
		}
		return result;
	}
	
	public static boolean isEmpty(byte[] src)
	{
		for (int i = 0; i < src.length; i++)
		{
			if (src[i] != 0) return false;
		}
		
		return true;
	}
}
