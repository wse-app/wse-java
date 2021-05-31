package wse.utils.types.xsd;

import java.math.BigInteger;

public class xsd_nonPositiveInteger extends xsd_integer {
	public xsd_nonPositiveInteger() {
		maxInclusive(BigInteger.ZERO);
	}
}