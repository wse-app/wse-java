package wse.utils;

public interface Transformer<F, T> {
	public T transform(F value);
}
