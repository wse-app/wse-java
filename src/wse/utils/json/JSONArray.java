package wse.utils.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import wse.utils.ArrayUtils;
import wse.utils.Transformer;

public class JSONArray extends JSONValue implements Iterable<Object> {

	private ArrayList<Object> internalList;

	public JSONArray() {
		this.internalList = new ArrayList<>();
	}

	public int size() {
		return internalList.size();
	}

	public boolean isEmpty() {
		return internalList.isEmpty();
	}

	public boolean containsKey(Object key) {
		return containsKey(Integer.parseInt(String.valueOf(key)));
	}

	public boolean containsKey(Integer index) {
		return index >= 0 && index < this.internalList.size();
	}

	public boolean containsKey(int index) {
		return index >= 0 && index < this.internalList.size();
	}

	public boolean containsValue(Object value) {
		return this.internalList.contains(value);
	}

	public <T> T get(int index) {
		return get(index, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInt(int index) {
		return (T) Integer.valueOf(get(index, String.class));
	}

	@SuppressWarnings("unchecked")
	public <T> T getLong(int index) {
		return (T) Long.valueOf(get(index, String.class));
	}

	@SuppressWarnings("unchecked")
	public <T> T getFloat(int index) {
		return (T) Float.valueOf(get(index, String.class));
	}

	@SuppressWarnings("unchecked")
	public <T> T getDouble(int index) {
		return (T) Double.valueOf(get(index, String.class));
	}

	@SuppressWarnings("unchecked")
	public <T> T getBool(int index) {
		return (T) Boolean.valueOf(get(index, String.class));
	}

	public JSONObject getObject(int index) {
		return (JSONObject) get(index, JSONObject.class);
	}

	public JSONArray getArray(int index) {
		return (JSONArray) get(index, JSONArray.class);
	}

	public <T> T get(int index, Class<T> cast) {
		if (index < 0 || index >= size())
			return null;
		return JSONUtils.cast(internalList.get(index), cast);
	}

	public <T> T add(int index, Object value) {
		if (index > size())
			index = size();
		if (!(value instanceof JSONValue))
			value = String.valueOf(value);
		internalList.add(index, value);
		return JSONUtils.cast(value);
	}

	/**
	 * 
	 * @param index
	 * @param value
	 * @return The value
	 */
	public JSONArray add(int index, JSONArray value) {
		if (index > size())
			index = size();
		internalList.add(index, value);
		return value;
	}

	/**
	 * 
	 * @param index
	 * @param value
	 * @return The value
	 */
	public JSONObject add(int index, JSONObject value) {
		if (index > size())
			index = size();
		internalList.add(index, value);
		return value;
	}

	/**
	 * 
	 * @param value
	 * @return This JSArray
	 */
	public JSONArray add(Object value) {
		internalList.add(value);
		return this;
	}

	/**
	 * 
	 * @param value
	 * @return The value
	 */
	public JSONArray add(JSONArray value) {
		internalList.add(value);
		return value;
	}

	/**
	 * 
	 * @param value
	 * @return The value
	 */
	public JSONObject add(JSONObject value) {
		internalList.add(value);
		return value;
	}

	public <T> T remove(Object key) {
		return remove(Integer.parseInt(String.valueOf(key)));
	}

	@SuppressWarnings("unchecked")
	public <T> T remove(Integer index) {
		Object o = get(index);
		internalList.remove(index);
		return (T) o;
	}

	public void clear() {
		internalList.clear();
	}

	public Collection<Object> values() {
		return internalList;
	}

	public Iterator<Object> iterator() {
		return internalList.iterator();
	}

	public Object[] toArray() {
		return internalList.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return internalList.toArray(a);
	}

	public boolean containsAll(Collection<? extends Object> c) {
		return internalList.containsAll(c);
	}

	/**
	 * 
	 * @param value
	 * @return This JSArray
	 */
	public JSONArray addAll(Object... value) {
		for (Object v : value)
			internalList.add(v);
		return this;
	}

	/**
	 * 
	 * @param output
	 * @return This JSArray
	 */
	public JSONArray addAll(Collection<? extends Object> c) {
		internalList.addAll(c);
		return this;
	}

	/**
	 * 
	 * @param output
	 * @return This JSArray
	 */
	public JSONArray addAll(int index, Collection<? extends Object> c) {
		internalList.addAll(index, c);
		return this;
	}

	public boolean removeAll(Collection<Object> values) {
		return internalList.removeAll(values);
	}

	public ListIterator<Object> listIterator() {
		return internalList.listIterator();
	}

	public ListIterator<Object> listIterator(int index) {
		return internalList.listIterator(index);
	}

	public List<Object> subList(int fromIndex, int toIndex) {
		return internalList.subList(fromIndex, toIndex);
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(final int level) {
		if (size() == 0)
			return "[]";
		return "[\n" + JSONUtils.level(level + 1)
				+ ArrayUtils.join(internalList, ",\n" + JSONUtils.level(level + 1), new Transformer<Object, String>() {
					@Override
					public String transform(Object value) {
						return JSONUtils.valueString(value, level + 1);
					}
				}) + "\n" + JSONUtils.level(level) + "]";
	}

}
