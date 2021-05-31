package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_gDay extends AnySimpleType<String> {
	public xsd_gDay() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return String.class;
	}

	@Override
	public String parse(String input) {
		return input;
	}
}