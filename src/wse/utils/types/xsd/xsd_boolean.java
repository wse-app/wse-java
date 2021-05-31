package wse.utils.types.xsd;

import wse.WSE;
import wse.utils.types.AnySimpleType;

public class xsd_boolean extends AnySimpleType<Boolean> {
	public xsd_boolean() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Boolean.class;
	}

	@Override
	public Boolean parse(String input) {
		return WSE.parseBool(input);
	}
}