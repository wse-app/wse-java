package wse.utils.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface IParser<T extends IElement> {
	public T parse(InputStream input, Charset cs) throws IOException;
	public T createEmpty();
}
