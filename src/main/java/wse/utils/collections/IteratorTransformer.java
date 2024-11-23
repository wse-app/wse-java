package wse.utils.collections;

import java.util.Iterator;

import wse.utils.Transformer;

public class IteratorTransformer<F, T> implements Iterator<T>, Iterable<T> {

	private Iterator<F> source;
	private Transformer<F, T> transformer;

	public IteratorTransformer(Iterator<F> it, Transformer<F, T> transformer) {
		this.source = it;
		this.transformer = transformer;
	}

	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	@Override
	public T next() {
		return transformer.transform(source.next());
	}

	@Override
	public void remove() {
		source.remove();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

}
