package wse.utils;

public class Suppliers {
	public static <T> Supplier<T> ofClass(final Class<T> clazz) {
		return new Supplier<T>() {
			@Override
			public T get() {
				try {
					return clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	public static <T> Supplier<T> ofInstance(final T t) {
		return new Supplier<T>() {
			@Override
			public T get() {
				return t;
			}
		};
	}
}
