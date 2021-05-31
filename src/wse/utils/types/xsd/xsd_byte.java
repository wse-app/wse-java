package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_byte extends AnySimpleType<Byte> {
	public xsd_byte() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Byte.class;
	}

	@Override
	public Byte parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return null;
		return Byte.parseByte(input);
	}
}