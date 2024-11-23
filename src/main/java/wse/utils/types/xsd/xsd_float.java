package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_float extends AnySimpleType<Float> {
	public xsd_float() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Float.class;
	}

	@Override
	public Float parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return Float.NaN;
		return Float.parseFloat(input);
	}
}