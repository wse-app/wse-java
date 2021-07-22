package wse.utils.json2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class JObject extends LinkedHashMap<String, Object> implements JValue {
	private static final long serialVersionUID = 7358202224561375529L;

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
		try {
			return (T) super.get(key);
		} catch (ClassCastException e) {
			System.out.println("??");
			return null;
		}
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
	
	public void put(String key, Collection<Object> valueArray) {
		super.put(key, new JArray(valueArray));
	}

	public String toString() {
		return prettyPrint().toString();
	}

	public byte[] toByteArray(Charset cs) {
		return toString().getBytes(cs);
	}

	public JStringBuilder prettyPrint() {
		return prettyPrint(0);
	}

	@Override
	public JStringBuilder prettyPrint(int level) {
		JStringBuilder builder = new JStringBuilder();
		prettyPrint(builder, level);
		return builder;
	}
	
	@Override
	public void prettyPrint(JStringBuilder builder, final int level) {
		
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
		
		while(it.hasNext()) {
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

	

}
