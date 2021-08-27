package wse.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClassUtils {
	private ClassUtils() {
	}

	public static String getJarFileLocation(Class<?> clazz) {
		File jarFile;
		try {
			CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
			jarFile = new File(codeSource.getLocation().toURI().getPath());
			String jarDir = jarFile.getParentFile().getPath();
			return jarDir;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File getJarFile(Class<?> clazz) {
		File jarFile;
		try {
			CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
			jarFile = new File(codeSource.getLocation().toURI().getPath());
			return jarFile;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getPackagePath(Class<?> clazz) {
		return clazz.getPackage().getName();
	}

	/**
	 * Checks if a method has the specified name, and the specified parameter types
	 * 
	 * @param m
	 * @param name  if null, name doesent matter
	 * @param types
	 * @return
	 */
	public static boolean validateMethod(Method m, String name, Class<?>... types) {
		if (name != null && !m.getName().equalsIgnoreCase(name))
			return false;
		Class<?>[] pt = m.getParameterTypes();
		if (pt.length != types.length)
			return false;
		for (int i = 0; i < pt.length; i++)
			if (!types[i].isAssignableFrom(pt[i])) {
				return false;
			}
		return true;
	}

	public static boolean isOverridden(Class<?> clazz, Method check) {
		Method m;
		try {
			m = clazz.getMethod(check.getName(), check.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
		if (m != null) {
			if (m.equals(check)) {
				return false;
			}
			return true;
		}
		return false;
	}

	public static Method getMethod(String name, Class<?> clazz, Class<?>... parameterTypes) {
		try {
			Method m = clazz.getMethod(name, parameterTypes);
			return m;
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Iterable<Class<?>> parentChain(final Class<?> clazz) {
		return new Iterable<Class<?>>() {
			@Override
			public Iterator<Class<?>> iterator() {
				return new Iterator<Class<?>>() {
					Class<?> current = clazz;

					@Override
					public boolean hasNext() {
						return current.getSuperclass() != null;
					}

					@Override
					public Class<?> next() {
						return (current = current.getSuperclass());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	public static <T extends Annotation> T getAnnotation(Method m, Class<T> clazz) {
		T result = m.getAnnotation(clazz);
		if (result != null)
			return result;

		Class<?> declaringClass = m.getDeclaringClass();
		for (Class<?> c : ClassUtils.parentChain(declaringClass)) {
			try {
				Method m_ = c.getMethod(m.getName(), m.getParameterTypes());
				result = m_.getAnnotation(clazz);
				if (result != null)
					return result;
			} catch (NoSuchMethodException | SecurityException e) {
			}
		}
		return null;
	}

	public static boolean isPrimitiveNumber(Class<?> clazz) {
		if (!clazz.isPrimitive())
			return false;
		if (clazz == int.class)
			return true;
		if (clazz == float.class)
			return true;
		if (clazz == long.class)
			return true;
		if (clazz == double.class)
			return true;
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static Collection<?> collectionInstance(Class<? extends Collection> it) {

		if (it.isInterface()) {
			try {
				if (Collection.class == it) {
					return LinkedList.class.newInstance();
				} else if (List.class == it) {
					return LinkedList.class.newInstance();
				} else if (Set.class == it) {
					return LinkedHashSet.class.newInstance();
				}
				// unknown sub-interface of Collection, we have no chance of knowing a suitable
				// implementation class
				return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				return it.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static final Map<Class<?>, Constructor> map = new HashMap<>();
	static {
		try {
			map.put(boolean.class, Boolean.class.getConstructor(String.class));
			map.put(char.class, Short.class.getConstructor(String.class));
			map.put(byte.class, Byte.class.getConstructor(String.class));
			map.put(short.class, Short.class.getConstructor(String.class));
			map.put(int.class, Integer.class.getConstructor(String.class));
			map.put(long.class, Long.class.getConstructor(String.class));
			map.put(float.class, Float.class.getConstructor(String.class));
			map.put(double.class, Double.class.getConstructor(String.class));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public static Object valueOfPrimitive(Class<?> primitiveClass, String value) {
		if (!primitiveClass.isPrimitive())
			return null;

		@SuppressWarnings("rawtypes")
		Constructor c = map.get(primitiveClass);
		if (c == null)
			return null;
		try {
			return c.newInstance(value);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		}
	}

}
