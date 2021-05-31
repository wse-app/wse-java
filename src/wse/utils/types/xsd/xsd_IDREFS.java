package wse.utils.types.xsd;

public class xsd_IDREFS extends xsd_list<String, xsd_IDREF> {
	public xsd_IDREFS() {
		super(String.class, xsd_IDREF.class);
	}
}