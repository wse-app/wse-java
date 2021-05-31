package wse.utils.types.xsd;

import java.math.BigInteger;

public class xsd_negativeInteger extends xsd_nonPositiveInteger {
	public xsd_negativeInteger() {
		maxInclusive(BigInteger.valueOf(-1));
	}
}