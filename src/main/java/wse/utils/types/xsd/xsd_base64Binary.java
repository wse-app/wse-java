package wse.utils.types.xsd;

import wse.WSE;
import wse.utils.types.AnySimpleType;

public class xsd_base64Binary extends AnySimpleType<byte[]> {
	public xsd_base64Binary() {
		whiteSpace("collapse", true);
	}

	@Override
	public String print(byte[] value) {
		return WSE.printBase64Binary(value);
	}

	@Override
	public Class<?> getBaseType() {
		return byte[].class;
	}

	@Override
	public byte[] parse(String input) {
		return WSE.parseBase64Binary(input);
	}
}