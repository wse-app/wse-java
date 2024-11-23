package wse.utils.types.xsd;

public class xsd_ENTITIES extends xsd_list<String, xsd_ENTITY> {
	public xsd_ENTITIES() {
		super(String.class, xsd_ENTITY.class);
	}
}