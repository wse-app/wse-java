package wse.utils.exception;

import java.util.Iterator;

public class WseException extends RuntimeException implements Iterable<Throwable> {
	private static final long serialVersionUID = 486529171433542386L;

	public WseException() {
		super();
	}

	public WseException(String message) {
		super(message);
	}

	public WseException(Throwable cause) {
		super(cause);
	}

	public WseException(String message, Throwable cause) {
		super(message, cause);
	}

	public WseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Throwable getRootCause() {
		return getRootCause(this);
	}

	/**
	 * Returns the last throwable in the getCause() chain.
	 * 
	 * @param t the parent throwable
	 * @return the first throwable that was thrown in this exception chain.
	 */
	public static Throwable getRootCause(Throwable t) {
		Throwable c = t.getCause();
		if (c == null)
			return t;
		return getRootCause(c);
	}

	public boolean isCausedBy(Class<? extends Throwable> cause) {
		return isCausedBy(this, cause);
	}

	public static boolean isCausedBy(Throwable first, Class<? extends Throwable> cause) {
		if (cause == null)
			return false;
		for (Throwable t : iterable(first)) {
			if (cause.isAssignableFrom(t.getClass()))
				return true;
		}
		return false;
	}

	/**
	 * Returns an iterator over elements of type Throwable, iterating over all
	 * throwables in the exception chain.
	 * 
	 * @return An iterator for each throwable in the getCause() chain.
	 */
	@Override
	public Iterator<Throwable> iterator() {
		return iterable(this).iterator();
	}

	/**
	 * Returns an iterable for elements of type Throwable, iterating over all
	 * throwables in the exception chain.
	 * 
	 * @return An iterable for each throwable in the getCause() chain.
	 */
	public static Iterable<Throwable> iterable(final Throwable first_) {
		return new Iterable<Throwable>() {
			@Override
			public Iterator<Throwable> iterator() {
				return new Iterator<Throwable>() {
					Throwable c = first_;

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public Throwable next() {
						try {
							return c;
						} finally {
							if (c != null)
								c = c.getCause();
						}
					}

					@Override
					public boolean hasNext() {
						return c != null;
					}
				};
			}
		};
	}

	private static String[] layers = new String[] { "\n", "\n\t", "\n\t\t", "\n\t\t\t", "\n\t\t\t\t", "\n\t\t\t\t\t",
			"\n\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t\t" };

	public static String getCauseTree(Throwable first) {
		StringBuilder b = new StringBuilder();

		int i = 0;
		for (Throwable t : iterable(first)) {
			if (i != 0)
				b.append(layers[i % layers.length]);
			b.append(t.getClass().getName());
			i++;
		}
		return b.toString();
	}

}
