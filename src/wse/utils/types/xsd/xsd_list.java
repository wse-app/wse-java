package wse.utils.types.xsd;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import wse.utils.ArrayUtils;
import wse.utils.exception.WseParsingException;
import wse.utils.types.AnySimpleType;

public abstract class xsd_list<F, T extends AnySimpleType<F>> extends AnySimpleType<Collection<F>> {

	private Class<F> baseType;
	private Class<T> simpleType;

	public xsd_list(Class<F> baseType, Class<T> simpleType) {
		this.baseType = baseType;
		this.simpleType = simpleType;
	}

	@Override
	public String print(Collection<F> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<F> validateInput(String input) {
		String[] split = input.split(" ");
		T type;
		try {
			type = simpleType.newInstance();
		} catch (Exception e) {
			throw new WseParsingException("Failed to instantiate " + simpleType.getName() + ": " + e.getMessage(),
					e);
		}
		List<F> result = new LinkedList<>();
		for (String v : split)
			try {
				result.add(type.validateInput(v));
			} catch (Exception e) {

			}
		return result;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String validateOutput(Object output_) {
		@SuppressWarnings("unchecked")
		Collection<F> output = (Collection<F>) output_;
		T type;
		try {
			type = simpleType.newInstance();
		} catch (Exception e) {
			throw new WseParsingException("Failed to instantiate " + simpleType.getName() + ": " + e.getMessage(),
					e);
		}
		List<String> result = new LinkedList<>();
		for (F f : output) {
			result.add(type.validateOutput(f));
		}
		return ArrayUtils.join(result, " ");
	}

	@Override
	public Object validateOutputGeneric(Object output) {
		return validateOutput(output);
	}
	
	@Override
	public Class<F> getBaseType() {
		return baseType;
	}

	@Override
	public List<F> parse(String input) {
		throw new UnsupportedOperationException();
	}

	protected final void enumeration(List<T> enumeration) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void fractionDigits(Integer fractionDigits) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void fractionDigits(Integer fractionDigits, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void length(Integer length) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void length(Integer length, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void maxExclusive(List<T> maxExclusive) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void maxExclusive(List<T> maxExclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void maxInclusive(List<T> maxInclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void maxInclusive(List<T> maxInclusive) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void maxLength(Integer maxLength) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void maxLength(Integer maxLength, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void minExclusive(List<T> minExclusive) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void minExclusive(List<T> minExclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void minInclusive(List<T> minInclusive) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void minInclusive(List<T> minInclusive, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void minLength(Integer minLength) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void minLength(Integer minLength, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void pattern(String pattern) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void totalDigits(Integer totalDigits) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void totalDigits(Integer totalDigits, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void whiteSpace(String whiteSpace) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}

	protected final void whiteSpace(String whiteSpace, boolean _fixed) {
		throw new UnsupportedOperationException("simpleType list can't have restrictions");
	}
}