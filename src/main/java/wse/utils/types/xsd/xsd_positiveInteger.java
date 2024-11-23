package wse.utils.types.xsd;

import java.math.BigInteger;

public class xsd_positiveInteger extends xsd_integer {
	public xsd_positiveInteger() {
		minInclusive(BigInteger.ONE);
	}
}