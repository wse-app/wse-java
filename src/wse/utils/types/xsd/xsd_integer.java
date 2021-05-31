package wse.utils.types.xsd;

import java.math.BigInteger;

import wse.utils.types.AnySimpleType;

public class xsd_integer extends AnySimpleType<BigInteger> {
	public xsd_integer() {
		whiteSpace("collapse", true);
	}

	@Override
	public Class<?> getBaseType() {
		return BigInteger.class;
	}

	@Override
	public BigInteger parse(String input) {
		if ("nan".equalsIgnoreCase(input))
			return null;
		return new BigInteger(input);
	}
}