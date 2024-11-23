package wse.utils.types.xsd;

import java.math.BigInteger;

public class xsd_unsignedLong extends xsd_integer {
	public xsd_unsignedLong() {
		minInclusive(BigInteger.valueOf(0L));
	}
}