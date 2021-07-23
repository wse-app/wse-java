package wse.utils.writable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class WriteableString implements StreamWriter
{

	public String value;

	public WriteableString(String value)
	{
		this.value = value;
	}
	
	public WriteableString(byte[] data)
	{
		this.value = new String(data);
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset)
	{
		if (value == null)
			return;
		try
		{
			stream.write(value.getBytes(charset));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
