package wse.utils.json;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import wse.utils.ArrayUtils;
import wse.utils.Transformer;
import wse.utils.writable.StreamWriter;

public class JSONObject extends JSONValue implements Iterable<Entry<String, Object>>, StreamWriter {

	private final Map<String, Object> internalMap;

	public JSONObject() {
		this(null);
	}

	protected JSONObject(Map<String, Object> copyOf) {
		this.internalMap = new HashMap<String, Object>();
		if (copyOf != null) {
			this.internalMap.putAll(copyOf);
		}
	}

	public int size() {
		return internalMap.size();
	}

	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return internalMap.containsKey(String.valueOf(key));
	}

	public boolean containsValue(Object value) {
		return internalMap.containsValue(value);
	}

	public <T> T get(String key) {
		return get(key, null);
	}

	public Integer getInt(String key) {
		return Integer.valueOf(get(key, String.class));
	}

	public Long getLong(String key) {
		return Long.valueOf(get(key, String.class));
	}

	public Float getFloat(String key) {
		return Float.valueOf(get(key, String.class));
	}

	public Double getDouble(String key) {
		return Double.valueOf(get(key, String.class));
	}

	public Boolean getBool(String key) {
		return Boolean.valueOf(get(key, String.class));
	}

	public JSONObject getObject(String key) {
		return (JSONObject) get(key, JSONObject.class);
	}

	public JSONArray getArray(String key) {
		return (JSONArray) get(key, JSONArray.class);
	}

	public <T> T get(String key, Class<T> cast) {
		return JSONUtils.cast(internalMap.get(String.valueOf(key)), cast);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return this JSObject
	 */
	public JSONObject put(String key, Object value) {
		internalMap.put(key, value);
		return this;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return The value
	 */
	public JSONArray put(String key, JSONArray value) {
		internalMap.put(key, value);
		return value;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return The value
	 */
	public JSONObject put(String key, JSONObject value) {
		internalMap.put(key, value);
		return value;
	}

	public <T> T remove(Object key) {
		return JSONUtils.cast(internalMap.remove(key));
	}

	public JSONObject putAll(Map<? extends String, ? extends Object> m) {
		internalMap.putAll(m);
		return this;
	}

	public void clear() {
		internalMap.clear();
	}

	public Set<String> keys() {
		return internalMap.keySet();
	}

	public Collection<Object> values() {
		return internalMap.values();
	}

	public Set<Entry<String, Object>> entrySet() {
		return internalMap.entrySet();
	}

	public Map<String, Object> toMap() {
		return internalMap;
	}

	public byte[] toByteArray(Charset set) {
		String s = toString();
		return s.getBytes(set);
	}
	
	public String toString() {
		return toString(0);
	}

	public String toString(final int level) {
		if (size() == 0)
			return "{}";
		return "{\n" + JSONUtils.level(level + 1) + ArrayUtils.join(internalMap.entrySet(),
				",\n" + JSONUtils.level(level + 1), new Transformer<Entry<String, Object>, String>() {
					@Override
					public String transform(Entry<String, Object> value) {
						return JSONUtils.keyString(value.getKey()) + ": "
								+ JSONUtils.valueString(value.getValue(), level + 1);
					}
				}) + "\n" + JSONUtils.level(level) + "}";
	}

	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return this.entrySet().iterator();
	}
}
