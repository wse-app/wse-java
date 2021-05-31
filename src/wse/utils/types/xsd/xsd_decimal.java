package wse.utils.types.xsd;

import java.math.BigDecimal;

import wse.utils.types.AnySimpleType;

public class xsd_decimal extends AnySimpleType<BigDecimal> {
	public xsd_decimal() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return BigDecimal.class;
	}

	@Override
	public BigDecimal parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return null;
		return new BigDecimal(input);
	}
}