package wse.utils.types.xsd;

public class xsd_language extends xsd_token {
	public xsd_language() {
		pattern("([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]{1,8})(-[a-zA-Z]{1,8})*");
	}
}