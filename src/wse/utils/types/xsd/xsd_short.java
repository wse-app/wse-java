package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_short extends AnySimpleType<Short> {
	public xsd_short() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Short.class;
	}

	@Override
	public Short parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return null;
		return Short.parseShort(input);
	}
}