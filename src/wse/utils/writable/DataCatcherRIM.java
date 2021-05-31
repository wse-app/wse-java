package wse.utils.writable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * RIM (Randomized Injection Mode) Data Catcher
 * 
 * 
 * @author WSE
 *
 */
public class DataCatcherRIM extends OutputStream
{

	Random random = new Random();

	byte[] buffer;
	int pointer = 0;
	final int injectionIndex;

	final int blockSize;
	final int vBlockSize;

	public DataCatcherRIM(int blockSize, int injectionIndex, final int bufferSize)
	{
		this.injectionIndex = Math.max(0, Math.min(blockSize - 1, injectionIndex)); // Clamp
		this.blockSize = blockSize;
		this.vBlockSize = blockSize - 1;

		int paddingSize = bufferSize;
		@SuppressWarnings("unused")
		int dataSize = bufferSize;
		int finalSize = bufferSize;

		if (blockSize > 1)
		{
			if ((bufferSize % (vBlockSize)) != 0)
			{
				// Add padding
				paddingSize = bufferSize + (vBlockSize - bufferSize % vBlockSize);
				// Padding size like normal as without RIM
			}

			int numBlocks = paddingSize / vBlockSize;
			// Actually how many bytes will be used for insertion

			finalSize = paddingSize + numBlocks;
		}

		buffer = new byte[finalSize];
	}

	public DataCatcherRIM(StreamWriter... wa)
	{
		this(-1, 0, wa);
	}

	public DataCatcherRIM(int blockSize, int injectionIndex, StreamWriter... wa)
	{
		this(blockSize, injectionIndex, SizeCatcher.getSize(wa));

		for (StreamWriter w : wa)
			try
			{
				w.writeToStream(this);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
	}

	public DataCatcherRIM(int blockSize, int injectionIndex, byte[] array)
	{
		this(blockSize, injectionIndex, array.length);
		write(array);
	}

	private void insertRandom()
	{
		// buffer[pointer++] = (byte) (random.nextInt(256) - 128);
		buffer[pointer++] = -128;
	}

	@Override
	public void write(int b) throws IOException
	{
		if (pointer % blockSize == injectionIndex)
			insertRandom();

		buffer[pointer] = (byte) b;
		pointer++;
	}

	@Override
	public void write(byte[] b)
	{
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len)
	{
		int blockPos = pointer % blockSize;
		int L = (blockSize - blockPos + injectionIndex) % blockSize;

		// Add everything up to random insertion

		if (L >= b.length)
		{
			System.arraycopy(b, off, buffer, pointer, b.length);
			pointer += b.length;
			return;
		}

		System.arraycopy(b, off, buffer, pointer, L);
		pointer += L;

		off += L;
		len -= L;

		// Add insertion and another vBlockSize bytes
		for (; len >= (vBlockSize);)
		{
			insertRandom();
			System.arraycopy(b, off, buffer, pointer, vBlockSize);
			pointer += (vBlockSize);

			len -= (vBlockSize);
			off += (vBlockSize);

		}

		if (len != 0)
		{
			// There are still leftovers that are to be in the new block, these
			// are fewer than vBlockSize, meaning not a full block
			insertRandom();
			System.arraycopy(b, off, buffer, pointer, len);
		}
		pointer += len;

	}

	public static byte[] getValue(int blockSize, int injectionIndex, StreamWriter... wa)
	{
		DataCatcherRIM c = new DataCatcherRIM(blockSize, injectionIndex, wa);
		try
		{
			c.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return c.getValue();
	}

	public byte[] getValue()
	{
		return buffer;
	}
}
