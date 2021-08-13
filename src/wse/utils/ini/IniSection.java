package wse.utils.ini;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import wse.utils.HasOptions;
import wse.utils.IOptions;
import wse.utils.MimeType;
import wse.utils.Options;
import wse.utils.internal.ILeaf;
import wse.utils.internal.StringGatherer;

public class IniSection extends LinkedHashMap<String, Object> implements ILeaf, HasOptions {

	private static final long serialVersionUID = -1330395256316531976L;
	private final Options options;

	public IniSection() {
		this.options = new Options();
	}

	private int row, column;

	@Override
	public int getRow() {
		return row;
	}

	@Override
	public int getColumn() {
		return column;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		prettyPrint().writeToStream(stream, charset);
	}

	@Override
	public StringGatherer prettyPrint() {
		return prettyPrint(0);
	}

	@Override
	public StringGatherer prettyPrint(int level) {
		StringGatherer gs = new StringGatherer();
		prettyPrint(gs, level);
		return gs;
	}

	@Override
	public void prettyPrint(StringGatherer builder, int level) {
		String sep = String.valueOf(options.get(IniOptions.KEY_VALUE_SEPARATOR));

		for (Map.Entry<String, Object> kv : this.entrySet()) {
			String key = kv.getKey();
			Object o = kv.getValue();

			if (o == null)
				continue;

			if (!(o instanceof Iterable)) {
				builder.add(key);
				builder.add(sep);
				builder.add(String.valueOf(kv.getValue()));
				builder.add("\r\n");
				continue;
			}

			Iterable<?> it = (Iterable<?>) o;
			for (Object ito : it) {
				builder.add(key);
				builder.add(sep);
				builder.add(String.valueOf(ito));
				builder.add("\r\n");
			}
		}
		
		if (this instanceof IniFile) {
			if (size() > 0)
				builder.add("\r\n");
		} else {
			builder.add("\r\n");
		}
	}

	@Override
	public String getValue(String key) {
		Object o = get(key);
		if (o == null)
			return null;

		if (!(o instanceof Iterable)) {
			return String.valueOf(o);
		}

		Iterator<?> it = ((Iterable<?>) o).iterator();
		if (!it.hasNext())
			return null;
		return String.valueOf(it.next());

	}

	@Override
	public String getValue(String key, String namespace) {
		return getValue(key);
	}

	@Override
	public void setValue(String key, Object value) {
		this.put(key, value);
	}

	@Override
	public void setValue(String key, String namespace, Object value) {
		setValue(key, value);
	}

	@Override
	public Collection<String> getValueArray(String key) {

		Object o = get(key);

		if (o == null)
			return Collections.emptyList();

		if (!(o instanceof Iterable)) {
			return Arrays.asList(String.valueOf(o));
		}

		Iterable<?> c = (Iterable<?>) o;

		List<String> result = new LinkedList<String>();
		for (Object co : c) {
			if (co != null)
				result.add(String.valueOf(co));
		}

		return result;
	}

	@Override
	public Collection<String> getValueArray(String key, String namespace) {
		return getValueArray(key);
	}

	@Override
	public void setValueArray(String key, Iterable<Object> value) {
		setValue(key, value);
	}

	@Override
	public void setValueArray(String key, String namespace, Iterable<Object> value) {
		setValueArray(key, value);
	}

	@Override
	public String getAttributeValue(String key) {
		return getValue(key);
	}

	@Override
	public String getAttributeValue(String key, String namespace) {
		return getAttributeValue(key);
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
	public Collection<String> getAttributeValueArray(String key) {
		return getValueArray(key);
	}

	@Override
	public Collection<String> getAttributeValueArray(String key, String namespace) {
		return getAttributeValueArray(key);
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
	public IOptions getOptions() {
		return options;
	}

	@Override
	public void setOptions(HasOptions other) {
		this.options.setOptions(other);
	}

	@Override
	public MimeType getMimeType() {
		return MimeType.text.plain;
	}

	@Override
	public Charset preferredCharset() {
		return Charset.forName("UTF-8");
	}

	@Override
	public String toString() {
		return prettyPrint().toString();
	}

}
