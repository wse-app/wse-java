package wse.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import wse.utils.exception.WseException;
import wse.utils.exception.WseParsingException;
import wse.utils.internal.IElement;
import wse.utils.json.JObject;
import wse.utils.types.AnySimpleType;
import wse.utils.types.AnyType;
import wse.utils.xml.XMLElement;

public abstract class ComplexType implements AnyType, Serializable {
	private static final long serialVersionUID = 1L;

	public void load(IElement src) {
		throw new WseException("Generated code outdated for this library version!");
	}

	public void create(IElement target) {
		throw new WseException("Generated code outdated for this library version!");
	}

	// To JSON

	public JObject toJSON() {
		JObject obj = new JObject();
		create(obj);
		return obj;
	}

	public XMLElement toXML() {
		XMLElement xml = new XMLElement(getClass().getSimpleName());
		create(xml);
		return xml;
	}

	// Dart bridge

	public Map<String, Object> toMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		Class<? extends ComplexType> clazz = getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			try {
				Object value = f.get(this);
				if (value == null)
					continue;

				if (List.class.isAssignableFrom(f.getType())) {
					List<?> list = (List<?>) value;
					List<Object> result = new LinkedList<>();

					for (Object o : list) {
						if (o == null)
							continue;
						if (o instanceof ComplexType) {
							o = ((ComplexType) o).toMap();
						}
						result.add(o);
					}
					value = result;
				} else if (ComplexType.class.isAssignableFrom(f.getType())) {
					value = ((ComplexType) value).toMap();
				}

				map.put(f.getName(), value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				continue;
			}
		}

		return map;

	}

	@SuppressWarnings("unchecked")
	public void fromMap(Map<String, Object> map) {
		Class<? extends ComplexType> clazz = getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			try {

				Object value = map.get(f.getName());
				if (value == null) {
					f.set(this, null);
					continue;
				}

				if (List.class.isAssignableFrom(f.getType())) {

					List<?> valueList = (List<?>) value;
					List<Object> resultList = new LinkedList<>();

					// Get generic type, i.e List<ComplexType> -> ComplexType
					ParameterizedType type = (ParameterizedType) f.getGenericType();
					Type genericType = type.getActualTypeArguments()[0];

					if (genericType instanceof Class && ComplexType.class.isAssignableFrom((Class<?>) genericType)) {
						Class<? extends ComplexType> typeClass = (Class<? extends ComplexType>) genericType;

						for (Object o : valueList) {
							ComplexType ctValue = typeClass.newInstance();

							Map<String, Object> valueMap = (Map<String, Object>) o;
							ctValue.fromMap(valueMap);
							resultList.add(ctValue);
						}
					} else {
						resultList.addAll(valueList);
					}

					value = resultList;

				} else if (ComplexType.class.isAssignableFrom(f.getType())) {
					ComplexType setValue = (ComplexType) f.getType().newInstance();
					setValue.fromMap((Map<String, Object>) value);
					value = setValue;
				}

				f.set(this, value);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				continue;
			}
		}
	}

	// toString
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + String.valueOf(toJSON());
	}
	
	// Generated Code Utils
	public static <T, F extends AnySimpleType<T>> T parse(String value, String name, Class<F> clazz, boolean mandatory,
			String default_) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		if (value == null) {
			if (default_ != null) {
				try {
					return simpleType.parse(default_);
				} catch (Exception e) {
					throw new WseParsingException(
							String.format("Failed to parse '%s' default value: %s", name, e.getMessage()), e);
				}
			} else if (mandatory) {
				throw new WseParsingException(String.format("Field '%s' is mandatory but was null", name));
			}

			return null;
		}

		try {
			return simpleType.validateInput(value);
		} catch (Exception e) {
			throw new WseParsingException(String.format("Failed to parse '%s': %s", name, e.getMessage()), e);
		}
	}

	public static <T, F extends AnySimpleType<T>> T parseFixed(Class<F> clazz, String value) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		return simpleType.parse(value);
	}

	public static <T, F extends AnySimpleType<T>> T parse(IElement parent, String name, String namespace,
			Class<F> clazz, boolean mandatory, String default_) {
		String value = parent.getValue(name, namespace);
		return parse(value, name, clazz, mandatory, default_);
	}

	public static <T, F extends AnySimpleType<T>> T parseAttribute(IElement parent, String name, String namespace,
			Class<F> clazz, boolean mandatory, String default_) {
		String value = parent.getAttributeValue(name, namespace);
		return parse(value, name, clazz, mandatory, default_);
	}

	public static <T, F extends AnySimpleType<T>> void print(IElement parent, String name, String namespace,
			Object value, Class<F> clazz, boolean mandatory, String default_) {
		Object out = print(value, name, clazz, mandatory, default_);
		if (out != null) {
			parent.setValue(name, namespace, out);
		}
	}

	public static <T, F extends AnySimpleType<T>> void printAttribute(IElement parent, String name, String namespace,
			Object value, Class<F> clazz, boolean mandatory, String default_) {
		Object out = print(value, name, clazz, mandatory, default_);
		if (out != null) {
			parent.setAttributeValue(name, namespace, out);
		}
	}

	public static <T, F extends AnySimpleType<T>> Object print(Object value, String name, Class<F> clazz,
			boolean mandatory, String default_) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		Object out = null;

		if (value == null) {
			if (default_ != null) {
				out = default_;
			} else if (mandatory) {
				throw new wse.utils.exception.WseBuildingException(
						String.format("Field '%s' is mandatory but was null", name));
			}
		} else {
			out = simpleType.validateOutputGeneric(value);
		}

		return out;
	}

	public static void printFixed(IElement parent, String value, String name, String namespace) {
		parent.setValue(name, namespace, value);
	}

	public static void printFixedAttribute(IElement parent, String value, String name, String namespace) {
		parent.setAttributeValue(name, namespace, value);
	}

	public static <T, F extends AnySimpleType<T>> List<T> parseList(IElement parent, String name, String namespace,
			Class<F> clazz, int min, Integer max) {

		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		Collection<String> values = parent.getValueArray(name, namespace);
		if (values == null)
			values = Collections.emptyList();

		if (values.size() < min || (max != null && values.size() > max)) {
			throw new wse.utils.exception.WseParsingException("Got unexpected number of '" + name + "': "
					+ values.size() + ", expected: " + min + (max == null ? "+" : ("-" + max)));
		}

		List<T> result = new LinkedList<>();
		for (String value : values) {
			result.add(simpleType.validateInput(value));
		}
		return result;
	}

	public static <T, F extends AnySimpleType<T>> void printList(IElement parent, String name, String namespace,
			Collection<?> values, Class<F> clazz, int min, Integer max) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);

		if (values == null)
			values = Collections.EMPTY_LIST;

		if (values.size() < min || (max != null && values.size() > max)) {
			throw new wse.utils.exception.WseBuildingException("Trying to send invalid amount of '" + name + "': "
					+ values.size() + ", should be: " + min + (max == null ? "+" : ("-" + max)));
		}

		Collection<Object> out = new LinkedList<>();

		for (Object o : values) {
			out.add(simpleType.validateOutputGeneric(o));
		}

		parent.setValueArray(name, namespace, out);
	}

	public static <T extends ComplexType> T parseComplex(IElement parent, String name, String namespace, Class<T> clazz,
			boolean mandatory) {
		IElement child = parent.getChild(name, namespace);
		if (child == null) {
			if (mandatory) {
				throw new WseParsingException(String.format("Type '%s' is mandatory but was null", name));
			}
			return null;
		}
		return parseComplex(child, clazz);
	}

	private static <T extends ComplexType> T parseComplex(IElement src, Class<T> clazz) {
		T type;
		try {
			type = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new WseParsingException("Failed to instantiate ComplexType class " + clazz.getName()
					+ ". Make sure this class has an available default constructor", e);
		}
		type.load(src);
		return type;
	}

	public static <T extends ComplexType> LinkedList<T> parseComplexList(IElement parent, String name, String namespace,
			Class<T> clazz, int min, Integer max) {

		Collection<IElement> children = parent.getChildArray(name, namespace);
		if (children.size() < min || (max != null && children.size() > max)) {
			throw new wse.utils.exception.WseParsingException("Got unexpected number of '" + name + "': "
					+ children.size() + ", expected: " + min + (max == null ? "+" : ("-" + max)));
		}
		LinkedList<T> list = new LinkedList<>();
		for (IElement xml : children) {
			list.add(parseComplex(xml, clazz));
		}
		return list;
	}

	public static <T extends ComplexType> void printComplex(IElement parent, String name, String namespace, T type,
			boolean mandatory) {
		if (type == null) {
			if (mandatory) {
				throw new wse.utils.exception.WseBuildingException(
						String.format("Type '%s' is mandatory but was null", name));
			}
			return;
		}

		IElement child = parent.createEmpty();

		type.create(child);

		parent.setChild(name, namespace, child);
	}

	public static <T extends ComplexType> void printComplexList(IElement parent, String name, String namespace,
			List<T> list, int min, Integer max) {
		if (list == null)
			list = Collections.emptyList();

		if (list.size() < min || (max != null && list.size() > max)) {
			throw new wse.utils.exception.WseParsingException("Got unexpected number of '" + name + "': " + list.size()
					+ ", expected: " + min + (max == null ? "+" : ("-" + max)));
		}

		Collection<IElement> children = new LinkedList<>();

		for (T type : list) {
			IElement child = parent.createEmpty();
			type.create(child);
			children.add(child);
		}

		parent.setChildArray(name, namespace, children);
	}
}
