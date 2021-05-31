package wse.utils.types.xsd;

import java.math.BigInteger;

public class xsd_nonNegativeInteger extends xsd_integer {
	public xsd_nonNegativeInteger() {
		minInclusive(BigInteger.ZERO);
	}
}