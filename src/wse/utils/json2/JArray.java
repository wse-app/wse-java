package wse.utils.json2;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class JArray extends LinkedList<Object> implements JValue {
	private static final long serialVersionUID = 1214660224200869636L;

	public JArray() {
		super();
	}
	
	public JArray(Object... elements) {
		this(Arrays.asList(elements));
	}
	
	public JArray(Collection<Object> elements) {
		super(elements);
	}
	
	@Override
	public byte[] toByteArray(Charset cs) {
		return toString().getBytes(cs);
	}

	@Override
	public String toString() {
		return prettyPrint().toString();
	}

	@Override
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
	public void prettyPrint(JStringBuilder builder, int level) {

		if (isEmpty()) {
			builder.add("[]");
			return;
		}

		builder.add("[\n");

		String lvl = JUtils.level(level + 1);
		builder.add(lvl);

		Iterator<Object> it = this.iterator();
		Object o = it.next();

		JUtils.addValueString(builder, level + 1, o);

		while (it.hasNext()) {
			o = it.next();

			builder.add(",\n");
			builder.add(lvl);
			JUtils.addValueString(builder, level + 1, o);
		}

		builder.add("\n");
		builder.add(JUtils.level(level));
		builder.add("]");

	}

}
