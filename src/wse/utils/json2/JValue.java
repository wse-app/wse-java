package wse.utils.json2;

import java.nio.charset.Charset;

public interface JValue {

	byte[] toByteArray(Charset cs);

	JStringBuilder prettyPrint();

	JStringBuilder prettyPrint(int level);
	
	void prettyPrint(JStringBuilder builder, int level);

}
