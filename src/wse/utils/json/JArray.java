package wse.utils.json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import wse.utils.internal.StringGatherer;

public class JArray extends LinkedList<Object> implements JValue {
	private static final long serialVersionUID = 1214660224200869636L;

	public JArray() {
		super();
	}

	public JArray(Object... elements) {
		this(Arrays.asList(elements));
	}

	public JArray(Iterable<Object> elements) {
		super();
		for (Object o : elements) {
			add(o);
		}
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
	public void prettyPrint(StringGatherer builder, int level) {

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

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		prettyPrint().writeToStream(stream, charset);
	}
}
