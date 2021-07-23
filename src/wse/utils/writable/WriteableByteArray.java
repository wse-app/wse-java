package wse.utils.writable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class WriteableByteArray implements StreamWriter
{

	public byte[] value;
	
	public WriteableByteArray(byte[] data)
	{
		this.value = data;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset)
	{
		if (value == null)
			return;
		try
		{
			stream.write(value);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
