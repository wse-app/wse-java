package wse.utils;

import java.util.Objects;

public class Option<T> {
	private final String name;
	private final T defValue;
	
	public Option() {
		this(null);
	}
	
	public Option(String name) {
		this(name, null);
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
	
	public T getDefaultValue() {
		return defValue;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
