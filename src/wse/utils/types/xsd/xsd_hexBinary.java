package wse.utils.types.xsd;

import wse.WSE;
import wse.utils.types.AnySimpleType;

public class xsd_hexBinary extends AnySimpleType<byte[]> {
	public xsd_hexBinary() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return String.class;
	}

	@Override
	public String print(byte[] value) {
		return WSE.printHexBinary(value);
	}

	@Override
	public byte[] parse(String input) {
		return WSE.parseHexBinary(input);
	}
}