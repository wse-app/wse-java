package wse.utils;

import java.util.Objects;

public class Option<T> {
	private final String name;
	private T defValue;
	private Supplier<T> defSupplier;

	public Option() {
		this(null);
	}

	public Option(String name) {
		this(name, (T) null);
	}

	public Option(Class<?> declaringClass, String name) {
		this(declaringClass, name, null);
	}

	public Option(Class<?> declaringClass, String name, T defValue) {
		this(declaringClass != null ? declaringClass.getName() + "." + name : name, defValue);
	}

	public Option(String name, T defValue) {
		this.name = name;
		this.defValue = defValue;
	}
	
	public Option(String name, Supplier<T> defValueSupplier) {
		this.name = name;
		this.defSupplier = defValueSupplier;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		return false;
	}

	public String getName() {
		return name;
	}

	public void setDefaultValue(T value) {
		this.defValue = value;
	}

	public void setDefaultValue(Supplier<T> supplier) {
		this.defSupplier = supplier;
	}

	public T getDefaultValue() {
		T result = null;
		if (this.defSupplier != null) {
			result = defSupplier.get();
		}

		if (result == null) {
			result = this.defValue;
		}

		return result;
	}

	@Override
	public String toString() {
		return name;
	}
}
