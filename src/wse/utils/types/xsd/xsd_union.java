package wse.utils.types.xsd;

import java.util.LinkedList;
import java.util.List;

import wse.utils.ArrayUtils;
import wse.utils.Transformer;
import wse.utils.exception.WseException;
import wse.utils.exception.WseParsingException;
import wse.utils.types.AnySimpleType;

public abstract class xsd_union extends AnySimpleType<Object> {

	private Class<? extends AnySimpleType<?>>[] simpleTypes;
	private List<AnySimpleType<?>> instances;

	private List<AnySimpleType<?>> instances() {
		try {
			if (this.instances == null) {
				this.instances = new LinkedList<>();
				for (Class<? extends AnySimpleType<?>> clazz : simpleTypes)
					this.instances.add(clazz.newInstance());
			}
			return this.instances;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new WseException("Failed to instanciate union types: " + e.getMessage(), e);
		}
	}

	@SafeVarargs
	public xsd_union(Class<? extends AnySimpleType<?>>... simpleTypes) {
		this.simpleTypes = simpleTypes;
	}

	@Override
	public Class<?> getBaseType() {
		return Object.class;
	}

	@Override
	public Object parse(String input) {
		for (int i = 0; i < simpleTypes.length; i++) {
			try {
				AnySimpleType<?> simpleType = simpleTypes[i].newInstance();
				return simpleType.validateInput(input);
			} catch (Exception e) {
				continue;
			}
		}
		throw new WseParsingException(
				"Union input '" + input + "' did not match any restrictions of any of the memberTypes: "
						+ ArrayUtils.join(simpleTypes, ", "));
	}

	public final void validateValueSpace(Object value) {
	}

	protected final void validateLexicalSpace(String value) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public String print(Object output) {

		for (AnySimpleType<?> simpleType : instances()) {
			try {
				if (simpleType.getBaseType() != output.getClass())
					continue;
				String valid = simpleType.validateOutput(output);
				return valid;
			} catch (Exception e) {
				continue;
			}
		}
		throw new WseParsingException(
				"Union output '" + output + "' did not match any restrictions of any of the memberTypes: "
						+ ArrayUtils.join(instances(), ", ", new Transformer<AnySimpleType<?>, String>() {
							@Override
							public String transform(AnySimpleType<?> value) {
								return value.getClass().getSimpleName() + ":" + value.getBaseType().getSimpleName();
							}
						}));
	}

	protected final void enumeration(Object enumeration) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void fractionDigits(Integer fractionDigits) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void fractionDigits(Integer fractionDigits, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void length(Integer length) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void length(Integer length, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void maxExclusive(Object maxExclusive) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void maxExclusive(Object maxExclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void maxInclusive(Object maxInclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void maxInclusive(Object maxInclusive) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void maxLength(Integer maxLength) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void maxLength(Integer maxLength, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void minExclusive(Object minExclusive) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void minExclusive(Object minExclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void minInclusive(Object minInclusive) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void minInclusive(Object minInclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void minLength(Integer minLength) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void minLength(Integer minLength, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void pattern(String pattern) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void totalDigits(Integer totalDigits) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void totalDigits(Integer totalDigits, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void whiteSpace(String whiteSpace) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}

	protected final void whiteSpace(String whiteSpace, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType union can't have restrictions");
	}
}