package wse.server;

public class SessionCounter {

	private final Object LOCK = new Object();
	private long counter = 0;

	public SessionCounter() {
	}

	public final void plus() {
		synchronized (LOCK) {
			counter++;
		}
	}

	public final void minus() {
		synchronized (LOCK) {
			counter--;
		}
	}

	public final long get() {
		return counter;
	}

}
