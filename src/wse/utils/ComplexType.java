package wse.utils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import wse.utils.exception.WseParsingException;
import wse.utils.json.JSONArray;
import wse.utils.json.JSONObject;
import wse.utils.types.AnySimpleType;
import wse.utils.types.AnyType;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public abstract class ComplexType implements AnyType, Serializable {
	private static final long serialVersionUID = 1L;

	public abstract void load(XMLElement xml);

	public abstract void create(XMLElement xml);

	// To JSON

	public JSONObject toJson() {
		return toJson(this, this.getClass());
	}

	public JSONObject toJson(Class<? extends ComplexType> interpretAs) {
		if (interpretAs.isAssignableFrom(this.getClass()) || this.getClass().isAssignableFrom(interpretAs))
			return toJson(this, interpretAs);
		throw new IllegalArgumentException(
				"Invalid class argument: must be a superclass or extension of this.getClass()");
	}

	public static JSONObject toJson(ComplexType type, Class<? extends ComplexType> clazz) {
		if (type == null)
			return null;
		if (clazz == null)
			clazz = type.getClass();
		JSONObject result = new JSONObject();
		for (Field f : clazz.getFields()) {
			appendField(result, f, type);
		}

		return result;
	}

	private static void appendField(JSONObject target, Field field, Object obj) {
		Class<?> type = field.getType();
		Object value;
		try {
			field.setAccessible(true);
			value = field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		if (value == null)
			return;

		Object result = jsonValueOf(type, value);

		target.put(field.getName(), result);
	}

	public static Object jsonValueOf(Class<?> type, Object value) {
		if (value == null)
			return null;
		if (type == null)
			type = value.getClass();
		Object result = null;

		if (type.isArray()) {
			JSONArray array = new JSONArray();

			int length = Array.getLength(value);
			Object o;
			for (int i = 0; i < length; i++) {
				o = Array.get(value, i);
				if (o == null)
					continue;
				array.add(jsonValueOf(o.getClass(), o));
			}

			result = array;

		} else if (ComplexType.class.isAssignableFrom(type)) {

			result = toJson((ComplexType) value, type.asSubclass(ComplexType.class));

		} else if (Iterable.class.isAssignableFrom(type)) {

			Iterable<?> collection = (Iterable<?>) value;
			JSONArray array = new JSONArray();
			Iterator<?> it = collection.iterator();

			Object o;
			while (it.hasNext()) {
				o = it.next();
				if (o == null)
					continue;
				array.add(jsonValueOf(o.getClass(), o));
			}

			result = array;
		} else {
			if (Number.class.isAssignableFrom(type) || ClassUtils.isPrimitiveNumber(type))
				result = value;
			else
				result = String.valueOf(value);
		}

		return result;
	}

	// From JSON

	public void fromJson(JSONObject json) {
		fromJson(this, this.getClass(), json);
	}

	public void fromJson(JSONObject json, Class<? extends ComplexType> interpretAs) {
		if (interpretAs.isAssignableFrom(this.getClass()) || this.getClass().isAssignableFrom(interpretAs))
			fromJson(this, interpretAs, json);
		throw new IllegalArgumentException(
				"Invalid class argument: must be a superclass or extension of this.getClass()");
	}

	public static void fromJson(ComplexType type, Class<? extends ComplexType> clazz, JSONObject json) {

		if (type == null || json == null)
			throw new NullPointerException("null");
		if (clazz == null)
			clazz = type.getClass();

		for (Field f : clazz.getFields()) {
			try {
				valueOf(type, f, json.get(f.getName()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void valueOf(ComplexType type, Field field, Object obj) throws Exception {
		Object value = parseJson(field.getGenericType(), obj);
		field.set(type, value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object parseJson(Type type, Object jsonValue) {

		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;

			if (clazz.isArray()) {
				if (!(jsonValue instanceof JSONArray))
					throw new RuntimeException("json element was not an array");

				JSONArray jsonArray = (JSONArray) jsonValue;

				Type arrayType = clazz.getComponentType();
				Object array = Array.newInstance((Class<?>) arrayType, jsonArray.size());

				int i = 0;
				for (Object jsonElement : jsonArray) {
					Array.set(array, i, parseJson(arrayType, jsonElement));
					i++;
				}

				return array;

			} else {

				if (((Class) type).isInstance(jsonValue))
					return jsonValue;

				if (clazz.isPrimitive()) {
					return ClassUtils.valueOfPrimitive((Class) type, String.valueOf(jsonValue));
				}

				try {
					Constructor constructor = clazz.getConstructor(String.class);
					return constructor.newInstance(String.valueOf(jsonValue));
				} catch (NoSuchMethodException e) {
					System.out.println("found no constructor(String) for " + clazz);
				} catch (Exception e) {
					return null;
				}

				try {
					Method m = clazz.getMethod("valueOf", String.class);
					return m.invoke(null, String.valueOf(jsonValue));
				} catch (NoSuchMethodException e) {
					System.out.println("found no valueOf(String) for " + clazz);
				} catch (Exception e) {
					return null;
				}

				return null;
			}
		} else if (type instanceof GenericArrayType) {
			if (!(jsonValue instanceof JSONArray))
				throw new RuntimeException("json element was not an array");

			JSONArray jsonArray = (JSONArray) jsonValue;

			GenericArrayType gat = (GenericArrayType) type;
			Class<?> raw = null;

			Type genericType = gat.getGenericComponentType();
			if (genericType instanceof Class) {
				raw = (Class) genericType;
			} else if (genericType instanceof ParameterizedType) {
				raw = (Class<?>) ((ParameterizedType) genericType).getRawType();
			} else if (genericType instanceof GenericArrayType) {
				// multi-dimensional array, ignore
				return null;
			}

			if (raw == null)
				return null;

			Object array = Array.newInstance(raw, jsonArray.size());

			int i = 0;
			for (Object jsonElement : jsonArray) {
				Array.set(array, i, parseJson(genericType, jsonElement));
				i++;
			}

			return array;

		} else if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Class<?> raw = (Class<?>) pt.getRawType();

			if (Collection.class.isAssignableFrom(raw)) {

				if (!(jsonValue instanceof JSONArray))
					throw new RuntimeException("json element was not an array");

				Type[] genericTypes = pt.getActualTypeArguments();
				if (genericTypes.length != 1)
					return null;

				Collection collection;

				collection = ClassUtils.collectionInstance(raw.asSubclass(Collection.class));

				for (Object jsonElement : ((JSONArray) jsonValue)) {
					collection.add(parseJson(genericTypes[0], jsonElement));
				}

				return collection;
			} else {

				// Parameterized type that is not a Collection.. ignore
				return null;
			}

		} else {
			throw new RuntimeException("Unknown type: " + type);
		}

	}

	// Generated Code Utils
	
	public static <T, F extends AnySimpleType<T>> T parse(XMLElement parent, String name, String namespace,
			Class<F> clazz, boolean mandatory, String default_) {
		String value = parent.getChildValue(name, namespace);
		return parse(value, name, clazz, mandatory, default_);
	}

	public static <T, F extends AnySimpleType<T>> T parse(String value, String name, Class<F> clazz, boolean mandatory,
			String default_) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		if (value == null) {
			if (default_ != null) {
				try {
					return simpleType.parse(default_);
				} catch(Exception e) {
					throw new WseParsingException(String.format("Failed to parse '%s' default value: %s", name, e.getMessage()), e);
				}
			} else if (mandatory) {
				throw new WseParsingException(String.format("Field '%s' is mandatory but was null", name));
			}
			
			return null;
		}

		try {
			return simpleType.validateInput(value);
		} catch(Exception e) {
			throw new WseParsingException(String.format("Failed to parse '%s': %s", name, e.getMessage()), e);
		}
	}

	public static <T, F extends AnySimpleType<T>> void print(XMLElement parent, String name, String namespace,
			Object value, Class<F> clazz, boolean mandatory, String default_) {
		String text = print(value, name, clazz, mandatory, default_);
		if (text != null) {
			parent.addChildValue(name, namespace, text);
		}

	}

	public static <T, F extends AnySimpleType<T>> String print(Object value, String name, Class<F> clazz,
			boolean mandatory, String default_) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		String text = null;

		if (value == null) {
			if (default_ != null) {
				text = default_;
			} else if (mandatory) {
				throw new wse.utils.exception.WseBuildingException(
						String.format("Field '%s' is mandatory but was null", name));
			}
		} else {
			text = simpleType.validateOutput(value);
		}

		return text;
	}

	public static <T, F extends AnySimpleType<T>> T parseFixed(Class<F> clazz, String value) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		return simpleType.parse(value);
	}

	public static void printFixed(XMLElement parent, String value, String name, String namespace) {
		parent.addChildValue(name, namespace, value);
	}

	public static <T, F extends AnySimpleType<T>> List<T> parseList(XMLElement parent, String name, String namespace,
			Class<F> clazz, int min, Integer max) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);
		List<XMLElement> values = parent.getChildren(name, namespace);

		if (values.size() < min || (max != null && values.size() > max)) {
			throw new wse.utils.exception.WseParsingException("Got unexpected number of '" + name + "': "
					+ values.size() + ", expected: " + min + (max == null ? "+" : ("-" + max)));
		}

		List<T> result = new LinkedList<>();
		for (String value : XMLUtils.values(values)) {
			result.add(simpleType.validateInput(value));
		}
		return result;
	}

	public static <T, F extends AnySimpleType<T>> void printList(XMLElement parent, String name, String namespace,
			Collection<?> values, Class<F> clazz, int min, Integer max) {
		AnySimpleType<T> simpleType = AnySimpleType.getInstance(clazz);

		if (values == null)
			values = Collections.EMPTY_LIST;

		if (values.size() < min || (max != null && values.size() > max)) {
			throw new wse.utils.exception.WseBuildingException("Trying to send invalid amount of '" + name + "': "
					+ values.size() + ", should be: " + min + (max == null ? "+" : ("-" + max)));
		}

		for (Object o : values) {
			parent.addChildValue(name, namespace, simpleType.validateOutput(o));
		}
	}

	public static <T extends ComplexType> T parseComplex(XMLElement parent, String name, String namespace,
			Class<T> clazz, boolean mandatory) {
		XMLElement xml = parent.getChild(name, namespace);
		if (xml == null) {
			if (mandatory) {
				throw new WseParsingException(String.format("Type '%s' is mandatory but was null", name));
			}
			return null;
		}
		return parseComplex(xml, clazz);
	}

	private static <T extends ComplexType> T parseComplex(XMLElement xml, Class<T> clazz) {
		T type;
		try {
			type = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new WseParsingException("Failed to instantiate ComplexType class " + clazz.getName()
					+ ". Make sure this class has an available default constructor", e);
		}
		type.load(xml);
		return type;
	}

	public static <T extends ComplexType> LinkedList<T> parseComplexList(XMLElement parent, String name,
			String namespace, Class<T> clazz, int min, Integer max) {
		List<XMLElement> children = parent.getChildren(name, namespace);
		if (children.size() < min || (max != null && children.size() > max)) {
			throw new wse.utils.exception.WseParsingException("Got unexpected number of '" + name + "': "
					+ children.size() + ", expected: " + min + (max == null ? "+" : ("-" + max)));
		}
		LinkedList<T> list = new LinkedList<>();
		for (XMLElement xml : children) {
			list.add(parseComplex(xml, clazz));
		}
		return list;
	}

	public static <T extends ComplexType> void printComplex(XMLElement parent, String name, String namespace, T type,
			boolean mandatory) {
		if (type == null) {
			if (mandatory) {
				throw new wse.utils.exception.WseBuildingException(
						String.format("Type '%s' is mandatory but was null", name));
			}
			return;
		}
		XMLElement child = parent.addChild(name, namespace);
		type.create(child);
	}

	public static <T extends ComplexType> void printComplexList(XMLElement parent, String name, String namespace,
			List<T> list, int min, Integer max) {
		if (list == null)
			list = Collections.emptyList();

		if (list.size() < min || (max != null && list.size() > max)) {
			throw new wse.utils.exception.WseParsingException("Got unexpected number of '" + name + "': " + list.size()
					+ ", expected: " + min + (max == null ? "+" : ("-" + max)));
		}

		for (T type : list) {
			XMLElement child = parent.addChild(name, namespace);
			type.create(child);
		}

	}

}
