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
		for (Throwable t : iterator(first)) {
			if (cause.isAssignableFrom(t.getClass()))
				return true;
		}
		return false;
	}

	@Override
	public Iterator<Throwable> iterator() {
		return iterator(this).iterator();
	}

	public static Iterable<Throwable> iterator(final Throwable first_) {
		return new Iterable<Throwable>() {
			@Override
			public Iterator<Throwable> iterator() {
				return new Iterator<Throwable>() {

					boolean first = true;
					Throwable c = first_;

					@Override
					public void remove() {
					}

					@Override
					public Throwable next() {
						if (first) {
							first = false;
							return c;
						}

						return c = c.getCause();
					}

					@Override
					public boolean hasNext() {
						return c.getCause() != null || first;
					}
				};
			}
		};
	}
	private static String[] layers = new String[] {"\n", "\n\t", "\n\t\t", "\n\t\t\t", "\n\t\t\t\t", "\n\t\t\t\t\t",
			"\n\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t", "\n\t\t\t\t\t\t\t\t\t" };
	public static String getCauseTree(Throwable first) {
		StringBuilder b = new StringBuilder();
		
		int i = 0;
		for (Throwable t : iterator(first)) {
			if (i != 0)
				b.append(layers[i]);
			b.append(t.getClass().getName());
			i++;
		}
		return b.toString();
	}

}
