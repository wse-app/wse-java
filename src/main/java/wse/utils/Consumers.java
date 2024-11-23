package wse.utils;

public class Consumers {
	@SuppressWarnings("rawtypes")
	static Consumer EMPTY = new Consumer() {
		@Override
		public void consume(Object value) {
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> Consumer<T> empty() {
		return (Consumer<T>) EMPTY;
	}
}
