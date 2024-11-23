package wse.utils.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import wse.WSE;
import wse.utils.ArrayUtils;
import wse.utils.exception.SimpleTypeRestrictionException;
import wse.utils.exception.WseBuildingException;
import wse.utils.exception.WseException;
import wse.utils.exception.WseParsingException;
import wse.utils.types.facet.WhiteSpace;

public abstract class AnySimpleType<T> implements AnyType {

	private static Map<Class<? extends AnySimpleType<?>>, AnySimpleType<?>> instanceMap = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T, F extends AnySimpleType<T>> F getInstance(Class<F> clazz) {
		if (instanceMap.containsKey(clazz))
			return (F) instanceMap.get(clazz);

		synchronized (clazz) {
			if (instanceMap.containsKey(clazz))
				return (F) instanceMap.get(clazz);
			try {
				F f = clazz.newInstance();
				instanceMap.put(clazz, f);
				return f;
			} catch (Exception e) {
				throw new WseException("Failed to initialize SimpleType: " + clazz.getName(), e);
			}
		}
	}

	protected AnySimpleType() {
		if (instanceMap.containsKey(this.getClass())) {
			WSE.getLogger().warning("Multiple instances of " + getClass().getName()
					+ " detected, use AnySimpleType.getInstance() instead");
		}
		restriction();
	}

	// lexical space
	protected List<List<String>> patterns = new LinkedList<>();
	protected String whiteSpace;

	protected Integer length;
	protected Integer maxLength;
	protected Integer minLength;
	protected Integer fractionDigits;
	protected Integer totalDigits;

	// value space
	protected List<T> enumeration = new LinkedList<>();
	protected T minInclusive;
	protected T maxInclusive;
	protected T minExclusive;
	protected T maxExclusive;

	public void restriction() {
		enumeration.clear();
		patterns.add(0, new LinkedList<String>());
	}

	public abstract Class<?> getBaseType();

	public String print(T value) {
		return String.valueOf(value);
	}

	public abstract T parse(String input);

	public T validateInput(String input) {
		boolean wasNull = input == null;
		if (input == null)
			input = "";
		try {
			validatePattern(input, this.patterns, this);
			validateLexicalSpace(input);
		} catch (Exception e) {
			throw new WseParsingException(
					this.getClass().getName() + " failed during lexical space control: " + e.getMessage(), e);
		}
		if (whiteSpace != null)
			input = WhiteSpace.parse(input, this.whiteSpace);
		T result;
		try {
			result = this.parse(input);
		} catch (Exception e) {
			throw new WseParsingException(this.getClass().getName()
					+ " failed during lexical to value space transformation: " + e.getMessage(), e);
		}
		try {
			validateValueSpace(result);
		} catch (Exception e) {
			throw new WseParsingException(
					this.getClass().getName() + " failed during value space control: " + e.getMessage(), e);
		}
		if (wasNull)
			result = null;
		return result;
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public String validateOutput(Object output) {
		T out;
		// allStrings
		if (output instanceof String)
			out = parse((String) output);
		else
			out = (T) output;

		try {
			validateValueSpace(out);
		} catch (Exception e) {
			throw new WseBuildingException(
					this.getClass().getName() + " failed during value space control: " + e.getMessage(), e);
		}
		String result;
		try {
			result = this.print(out);
		} catch (Exception e) {
			throw new WseBuildingException(this.getClass().getName()
					+ " failed during value to lexical space transformation: " + e.getMessage(), e);
		}
		try {
			validateLexicalSpace(result);
		} catch (Exception e) {
			throw new WseBuildingException(
					this.getClass().getName() + " failed during lexical space control: " + e.getMessage(), e);
		}
		if (whiteSpace != null)
			result = WhiteSpace.parse(result, this.whiteSpace);
		return result;
	}

	@SuppressWarnings("unchecked")
	public Object validateOutputGeneric(Object output) {
		T out;
		// allStrings
		if (output instanceof String)
			out = parse((String) output);
		else
			out = (T) output;

		try {
			validateValueSpace(out);
		} catch (Exception e) {
			throw new WseBuildingException(
					this.getClass().getName() + " failed during value space control: " + e.getMessage(), e);
		}

		String result = this.print(out);

		if (this.length == null && this.minLength == null && this.maxLength == null) {
			return result;
		}

		try {
			validateLexicalSpace(result);
		} catch (Exception e) {
			throw new WseBuildingException(
					this.getClass().getName() + " failed during lexical space control: " + e.getMessage(), e);
		}
		return result;

	}

	public void validateValueSpace(T value) {
		if (!validateEnumeration(value, this.enumeration))
			throw new SimpleTypeRestrictionException(this, value, "enumeration",
					ArrayUtils.join(this.enumeration, ", "));
		if (!validateMinInclusive(value, this.minInclusive))
			throw new SimpleTypeRestrictionException(this, value, "minInclusive", this.minInclusive);
		if (!validateMaxInclusive(value, this.maxInclusive))
			throw new SimpleTypeRestrictionException(this, value, "maxInclusive", this.maxInclusive);
		if (!validateMinExclusive(value, this.minExclusive))
			throw new SimpleTypeRestrictionException(this, value, "minExclusive", this.minExclusive);
		if (!validateMaxExclusive(value, this.maxExclusive))
			throw new SimpleTypeRestrictionException(this, value, "maxExclusive", this.maxExclusive);
		if (!validateTotalDigits(value, this.totalDigits))
			throw new SimpleTypeRestrictionException(this, value, "totalDigits", this.totalDigits);
		if (!validateFractionDigits(value, this.fractionDigits))
			throw new SimpleTypeRestrictionException(this, value, "fractionDigits", this.fractionDigits);
	}

	protected void validateLexicalSpace(String value) {
		if (!validateLength(value, this.length))
			throw new SimpleTypeRestrictionException(this, value, "length", this.length);
		if (!validateMinLength(value, this.minLength))
			throw new SimpleTypeRestrictionException(this, value, "minLength", this.minLength);
		if (!validateMaxLength(value, this.maxLength))
			throw new SimpleTypeRestrictionException(this, value, "maxLength", this.maxLength);
	}

	protected static boolean validatePattern(String value, List<List<String>> patterns, AnySimpleType<?> simpleType) {
		if (patterns == null || patterns.isEmpty())
			return true;
		for (List<String> step : patterns) {
			boolean any = step.isEmpty();
			for (String pattern : step) {
				if (String.valueOf(value).matches(pattern)) {
					any = true;
					break;
				}
				System.out.println("'" + value + "' did not match " + pattern);
			}
			if (!any)
				if (simpleType != null)
					throw new SimpleTypeRestrictionException(simpleType, value, ArrayUtils.join(step, " OR "));
				else
					return false;
		}
		return true;
	}

	protected static boolean validateLength(String value, Integer length) {
		return (length == null || (value == null ? 0 : value.length()) == length);
	}

	protected static boolean validateMinLength(String value, Integer minLength) {
		return (minLength == null || (value == null ? 0 : value.length()) >= minLength);
	}

	protected static boolean validateMaxLength(String value, Integer maxLength) {
		return (maxLength == null || (value == null ? 0 : value.length()) <= maxLength);
	}

	protected static <T> boolean validateEnumeration(T value, Collection<T> enumerations) {
		if (enumerations == null || enumerations.isEmpty())
			return true;
		for (T e : enumerations) {
			if (Objects.equals(e, value))
				return true;
		}
		return false;
	}

	protected static <T> boolean validateMinInclusive(T value, T minInclusive) {
		if (minInclusive == null)
			return true;
		if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float) {
				return ((Number) value).doubleValue() >= ((Number) minInclusive).doubleValue();
			}
			if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
				return ((Number) value).longValue() >= ((Number) minInclusive).longValue();
			}
			if (value instanceof BigInteger) {
				return ((BigInteger) value).compareTo((BigInteger) minInclusive) >= 0;
			}
			if (value instanceof BigDecimal) {
				return ((BigDecimal) value).compareTo((BigDecimal) minInclusive) >= 0;
			}
		}
		return false;
	}

	protected static <T> boolean validateMaxInclusive(T value, T maxInclusive) {
		if (maxInclusive == null)
			return true;
		if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float) {
				return ((Number) value).doubleValue() <= ((Number) maxInclusive).doubleValue();
			}
			if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
				return ((Number) value).longValue() <= ((Number) maxInclusive).longValue();
			}
			if (value instanceof BigInteger) {
				return ((BigInteger) value).compareTo((BigInteger) maxInclusive) <= 0;
			}
			if (value instanceof BigDecimal) {
				return ((BigDecimal) value).compareTo((BigDecimal) maxInclusive) <= 0;
			}
		}
		return false;
	}

	protected static <T> boolean validateMinExclusive(T value, T minExclusive) {
		if (minExclusive == null)
			return true;
		if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float) {
				return ((Number) value).doubleValue() > ((Number) minExclusive).doubleValue();
			}
			if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
				return ((Number) value).longValue() > ((Number) minExclusive).longValue();
			}
			if (value instanceof BigInteger) {
				return ((BigInteger) value).compareTo((BigInteger) minExclusive) > 0;
			}
			if (value instanceof BigDecimal) {
				return ((BigDecimal) value).compareTo((BigDecimal) minExclusive) > 0;
			}
		}
		return false;
	}

	protected static <T> boolean validateMaxExclusive(T value, T maxExclusive) {
		if (maxExclusive == null)
			return true;
		if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float) {
				return ((Number) value).doubleValue() < ((Number) maxExclusive).doubleValue();
			}
			if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
				return ((Number) value).longValue() < ((Number) maxExclusive).longValue();
			}
			if (value instanceof BigInteger) {
				return ((BigInteger) value).compareTo((BigInteger) maxExclusive) < 0;
			}
			if (value instanceof BigDecimal) {
				return ((BigDecimal) value).compareTo((BigDecimal) maxExclusive) < 0;
			}
		}
		return false;
	}

	protected static <T> boolean validateFractionDigits(T value, Integer fractionDigits) {
		if (fractionDigits == null)
			return true;
		if (value instanceof Number) {
			if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
				return 0 <= fractionDigits;
			}
			if (value instanceof BigInteger) {
				return 0 <= fractionDigits;
			}
			String text = String.valueOf(value);
			if (text.contains(".")) {
				text = text.substring(text.indexOf('.') + 1).replaceAll("(0*\\D+\\d*)$", "");
				return text.length() <= fractionDigits;
			} else {
				return 0 <= fractionDigits;
			}
		}
		return false;
	}

	protected static <T> boolean validateTotalDigits(T value, Integer totalDigits) {
		if (totalDigits == null)
			return true;
		if (value instanceof Number) {
			String text = String.valueOf(value);
			text = text.replaceAll("\\D*", "");
			return text.length() <= totalDigits;
		}
		return false;
	}

	private boolean _fixedMinInclusive = false;

	protected void minInclusive(T minInclusive) {
		minInclusive(minInclusive, false);
	}

	protected void minInclusive(T minInclusive, boolean _fixed) {
		if (!_fixedMinInclusive) {
			this.minInclusive = minInclusive;
			this._fixedMinInclusive = _fixedMinInclusive || _fixed;
		}
	}

	private boolean _fixedMaxInclusive = false;

	protected void maxInclusive(T maxInclusive) {
		maxInclusive(maxInclusive, false);
	}

	protected void maxInclusive(T maxInclusive, boolean _fixed) {
		if (!_fixedMaxInclusive) {
			this.maxInclusive = maxInclusive;
			this._fixedMaxInclusive = _fixedMaxInclusive || _fixed;
		}
	}

	private boolean _fixedMinExclusive = false;

	protected void minExclusive(T minExclusive) {
		minExclusive(minExclusive, false);
	}

	protected void minExclusive(T minExclusive, boolean _fixed) {
		if (!_fixedMinExclusive) {
			this.minExclusive = minExclusive;
			this._fixedMinExclusive = _fixedMinExclusive || _fixed;
		}
	}

	private boolean _fixedMaxExclusive = false;

	protected void maxExclusive(T maxExclusive) {
		maxExclusive(maxExclusive, false);
	}

	protected void maxExclusive(T maxExclusive, boolean _fixed) {
		if (!_fixedMaxExclusive) {
			this.maxExclusive = maxExclusive;
			this._fixedMaxExclusive = _fixedMaxExclusive || _fixed;
		}
	}

	protected void pattern(String pattern) {

		if (pattern == null)
			return;

		List<String> step = patterns.size() > 0 ? patterns.get(0) : null;
		if (step == null) {
			step = new LinkedList<>();
			patterns.add(0, step);
		}

		pattern = pattern.replace((CharSequence) "\\c", "[-._:A-Za-z0-9]");
		step.add(pattern);

	}

	private boolean _fixedWhiteSpace = false;

	protected void whiteSpace(String whiteSpace) {
		whiteSpace(whiteSpace, false);
	}

	protected void whiteSpace(String whiteSpace, boolean _fixed) {
		if (!_fixedWhiteSpace) {
			this.whiteSpace = whiteSpace;
			this._fixedWhiteSpace = _fixedWhiteSpace || _fixed;
		}
	}

	private boolean _fixedLength = false;

	protected void length(Integer length) {
		length(length, false);
	}

	protected void length(Integer length, boolean _fixed) {
		if (!_fixedLength) {
			this.length = length;
			this._fixedLength = _fixedLength || _fixed;
		}
	}

	private boolean _fixedFractionDigits = false;

	protected void fractionDigits(Integer fractionDigits) {
		length(fractionDigits, false);
	}

	protected void fractionDigits(Integer fractionDigits, boolean _fixed) {
		if (!_fixedFractionDigits) {
			this.fractionDigits = fractionDigits;
			this._fixedFractionDigits = _fixedFractionDigits || _fixed;
		}
	}

	private boolean _fixedTotalDigits = false;

	protected void totalDigits(Integer totalDigits) {
		length(totalDigits, false);
	}

	protected void totalDigits(Integer totalDigits, boolean _fixed) {
		if (!_fixedTotalDigits) {
			this.totalDigits = totalDigits;
			this._fixedTotalDigits = _fixedTotalDigits || _fixed;
		}
	}

	private boolean _fixedMaxLength = false;

	protected void maxLength(Integer maxLength) {
		maxLength(maxLength, false);
	}

	protected void maxLength(Integer maxLength, boolean _fixed) {
		if (!_fixedMaxLength) {
			this.maxLength = maxLength;
			this._fixedMaxLength = _fixedMaxLength || _fixed;
		}
	}

	private boolean _fixedMinLength = false;

	protected void minLength(Integer minLength) {
		minLength(minLength, false);
	}

	protected void minLength(Integer minLength, boolean _fixed) {
		if (!_fixedMinLength) {
			this.minLength = minLength;
			this._fixedMinLength = _fixedMinLength || _fixed;
		}
	}

	protected void enumeration(T[] enumeration) {
		for (T e : enumeration)
			this.enumeration.add(e);
	}

	protected void enumeration(T enumeration) {
		this.enumeration.add(enumeration);
	}

	// TYPES

}
