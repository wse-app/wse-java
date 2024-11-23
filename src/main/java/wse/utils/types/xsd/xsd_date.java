package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_date extends AnySimpleType<String> {
	public xsd_date() {
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