package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_gMonthDay extends AnySimpleType<String> {
	public xsd_gMonthDay() {
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