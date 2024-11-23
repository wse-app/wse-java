package wse.utils.exception;

import wse.utils.types.AnySimpleType;

public class SimpleTypeRestrictionException extends SimpleTypeException {

	private static final long serialVersionUID = -2918516613404520900L;

	public SimpleTypeRestrictionException(AnySimpleType<?> simpleType, Object value) {
		super("simpleType '" + simpleType.getClass().getSimpleName() + "' with value '" + value
				+ "' did not match its restrictions");
	}

	public SimpleTypeRestrictionException(AnySimpleType<?> simpleType, Object value, String restrictionType) {
		super("simpleType '" + simpleType.getClass().getSimpleName() + "' with value '" + value
				+ "' did not match its '" + restrictionType + "' restriction");
	}

	public SimpleTypeRestrictionException(AnySimpleType<?> simpleType, Object value, String restrictionType,
			Object restrictionValue) {
		super("simpleType '" + simpleType.getClass().getSimpleName() + "' with value '" + value
				+ "' did not match its restriction: '" + restrictionType + "': '" + String.valueOf(restrictionValue)
				+ "'");
	}

}
