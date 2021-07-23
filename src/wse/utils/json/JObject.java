package wse.utils.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import wse.utils.MimeType;
import wse.utils.internal.IElement;

public class JObject extends LinkedHashMap<String, Object> implements JValue, IElement {
	private static final long serialVersionUID = 7358202224561375529L;

	private int row, column;

	public JObject() {
	}

	public JObject(Map<String, ? extends Object> copyOf) {

		this.putAll(copyOf);
	}

	public static <T> T parse(InputStream input, Charset cs) throws IOException {
		return JTokenizer.parse(input, cs);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return (T) super.get(key);
	}

	public Class<?> typeof(String key) {
		Object value = super.get(key);
		if (value == null)
			return null;
		return value.getClass();
	}

	public void put(String key, Object... valueArray) {
		this.put(key, Arrays.asList(valueArray));
	}

	public void put(String key, Iterable<Object> valueArray) {
		super.put(key, new JArray(valueArray));
	}

	public String toString() {
		return prettyPrint().toString();
	}

	public byte[] toByteArray(Charset cs) {
		return toString().getBytes(cs);
	}

	public StringGatherer prettyPrint() {
		return prettyPrint(0);
	}

	@Override
	public StringGatherer prettyPrint(int level) {
		StringGatherer builder = new StringGatherer();
		prettyPrint(builder, level);
		return builder;
	}

	@Override
	public void prettyPrint(StringGatherer builder, final int level) {

		Iterator<java.util.Map.Entry<String, Object>> it = this.entrySet().iterator();

		if (!it.hasNext()) {
			builder.add("{}");
			return;
		}

		builder.add("{\n");
		String lvl = JUtils.level(level + 1);
		builder.add(lvl);

		java.util.Map.Entry<String, Object> e = it.next();

		builder.add(JUtils.quoted(e.getKey()));
		builder.add(": ");
		JUtils.addValueString(builder, level + 1, e.getValue());

		while (it.hasNext()) {
			e = it.next();

			builder.add(",\n");
			builder.add(lvl);

			builder.add(JUtils.quoted(e.getKey()));
			builder.add(": ");
			JUtils.addValueString(builder, level + 1, e.getValue());
		}

		builder.add("\n");
		builder.add(JUtils.level(level));
		builder.add("}");
	}

	@Override
	public int getRow() {
		return row;
	}

	@Override
	public int getColumn() {
		return column;
	}

	protected void setPos(int row, int column) {
		this.row = row;
		this.column = column;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		prettyPrint().writeToStream(stream, charset);
	}

	@Override
	public String getValue(String key, String namespace) {
		return getValue(key);
	}

	@Override
	public String getValue(String key) {
		return String.valueOf((Object) get(key));
	}

	@Override
	public JObject getChild(String key, String namespace) {
		return getChild(key);
	}

	@Override
	public JObject getChild(String key) {
		Object o = get(key);

		if (o == null)
			return null;

		if (o instanceof JObject)
			return (JObject) o;

		throw new JException(this, String.format("Child '%s' is not an object", key));
	}

	@Override
	public Collection<String> getValueArray(String key, String namespace) {
		return getValueArray(key);
	}

	@Override
	public Collection<String> getValueArray(String key) {
		Object o = get(key);

		if (o == null)
			return null;

		if (!(o instanceof JArray))
			throw new JException(this, String.format("Child '%s' is not an array", key));

		JArray a = (JArray) o;
		Collection<String> result = new LinkedList<>();

		for (Object ao : a) {
			if (ao instanceof JValue)
				throw new JException(this,
						String.format("Child '%s' contains invalid element type, expected only raw values", key));

			result.add(String.valueOf(ao));
		}

		return result;
	}

	@Override
	public Collection<IElement> getChildArray(String key, String namespace) {
		return getChildArray(key);
	}

	@Override
	public Collection<IElement> getChildArray(String key) {
		Object o = get(key);

		if (o == null)
			return null;

		if (!(o instanceof JArray))
			throw new JException(this, String.format("Child '%s' is not an array", key));

		JArray a = (JArray) o;
		Collection<IElement> result = new LinkedList<>();

		for (Object ao : a) {
			if (!(ao instanceof JObject))
				throw new JException(this,
						String.format("Child '%s' contains invalid element type, expected only objects", key));
			result.add((JObject) ao);
		}

		return result;
	}

	@Override
	public String getAttributeValue(String key) {
		return getValue(key);
	}

	@Override
	public String getAttributeValue(String key, String namespace) {
		return getValue(key, namespace);
	}

	@Override
	public IElement createEmptyChild() {
		return new JObject();
	}

	@Override
	public void setValue(String key, Object value) {
		put(key, value);
	}

	@Override
	public void setValue(String key, String namespace, Object value) {
		setValue(key, value);
	}

	@Override
	public void setValueArray(String key, Iterable<Object> value) {
		put(key, value);
	}

	@Override
	public void setValueArray(String key, String namespace, Iterable<Object> value) {
		setValueArray(key, value);

	}

	@Override
	public void setAttributeValue(String key, Object value) {
		setValue(key, value);
	}

	@Override
	public void setAttributeValue(String key, String namespace, Object value) {
		setAttributeValue(key, value);
	}

	@Override
	public void setAttributeValueArray(String key, Iterable<Object> value) {
		setValueArray(key, value);
	}

	@Override
	public void setAttributeValueArray(String key, String namespace, Iterable<Object> value) {
		setAttributeValueArray(key, value);
	}

	@Override
	public void setChild(String key, IElement child) {
		put(key, child);
	}

	@Override
	public void setChild(String key, String namespace, IElement child) {
		setChild(key, child);
	}

	@Override
	public void setChildArray(String key, Iterable<IElement> children) {
		Collection<Object> c = new LinkedList<>();
		for (IElement e : children)
			c.add(e);
		put(key, new JArray(c));
	}

	@Override
	public void setChildArray(String key, String namespace, Iterable<IElement> children) {
		setChildArray(key, children);
	}

	
	@Override
	public MimeType getMimeType() {
		return MimeType.application.json;
	}

}
