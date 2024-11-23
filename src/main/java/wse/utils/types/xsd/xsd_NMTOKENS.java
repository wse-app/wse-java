package wse.utils.types.xsd;

public class xsd_NMTOKENS extends xsd_list<String, xsd_NMTOKEN> {
	public xsd_NMTOKENS() {
		super(String.class, xsd_NMTOKEN.class);
	}
}