package wse.utils.types.xsd;

import wse.utils.types.AnySimpleType;

public class xsd_double extends AnySimpleType<Double> {
	public xsd_double() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return Double.class;
	}

	@Override
	public Double parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return Double.NaN;
		return Double.parseDouble(input);
	}
}