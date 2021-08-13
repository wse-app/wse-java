package wse.utils.json;

import java.nio.charset.Charset;

import wse.utils.internal.StringGatherer;
import wse.utils.writable.StreamWriter;

public interface JValue extends StreamWriter {

	byte[] toByteArray(Charset cs);

	StringGatherer prettyPrint();

	StringGatherer prettyPrint(int level);
	
	void prettyPrint(StringGatherer builder, int level);

}
