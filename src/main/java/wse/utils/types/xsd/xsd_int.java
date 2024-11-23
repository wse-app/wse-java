package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_int extends AnySimpleType<Integer> {
	public xsd_int() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Integer.class;
	}

	@Override
	public Integer parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return null;
		return Integer.parseInt(input);
	}
}