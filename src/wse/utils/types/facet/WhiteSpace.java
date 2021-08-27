package wse.utils.types.facet;

import wse.utils.StringUtils;

public enum WhiteSpace {

	preserve, replace, collapse;

	public String parse(String value) {
		return parse(value, this);
	}

	public static String parse(String value, String whitespace) {
		if (whitespace == null)
			return value;
		return parse(value, WhiteSpace.valueOf(whitespace));
	}

	public static String parse(String value, WhiteSpace whitespace) {
		switch (whitespace) {
		case collapse:
			return StringUtils.WS_collapse(value);
		case replace:
			return StringUtils.WS_replace(value);
		case preserve:
		default:
			return value;

		}
	}
}
