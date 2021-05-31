package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_long extends AnySimpleType<Long> {
	public xsd_long() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Long.class;
	}

	@Override
	public Long parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return null;
		return Long.parseLong(input);
	}
}