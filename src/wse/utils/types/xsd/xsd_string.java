package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_string extends AnySimpleType<String> {
	@Override
	public Class<?> getBaseType() {
		return String.class;
	}

	@Override
	public String parse(String input) {
		return input;
	}
	
	
}