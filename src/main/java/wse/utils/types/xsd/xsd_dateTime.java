package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_dateTime extends AnySimpleType<String> {
	public xsd_dateTime() {
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