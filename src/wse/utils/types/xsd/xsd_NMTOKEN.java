package wse.utils.types.xsd;

public class xsd_NMTOKEN extends xsd_token {
	public xsd_NMTOKEN() {
		pattern("\\c+");
	}
}