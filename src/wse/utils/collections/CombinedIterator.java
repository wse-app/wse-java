package wse.utils.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class CombinedIterator<T> implements Iterator<T>{
	public static interface Controller {
		public void empty();
	}
	
	
	private LinkedList<Iterator<T>> iterators = new LinkedList<>();
	private Iterator<T> current;
	
	private Controller controller;
	
	@SafeVarargs
	public CombinedIterator(Controller controller, Iterator<T>... iterators) {
		this.controller = controller;
		this.iterators.addAll(Arrays.asList(iterators));
	}
	
	@Override
	public boolean hasNext() {
		if (current == null) {
			
		}
		while(current == null || !current.hasNext()) {
			if (iterators.isEmpty()) {
				if (this.controller != null) {
					controller.empty();
					if (iterators.isEmpty())
						return false;
				}
				return false;
			}
			current = iterators.pop();
		}
		return true;
	}

	@Override
	public T next() {
		return current.next();
	}

	@Override
	public void remove() {
		current.remove();
	}
	
	@SuppressWarnings("unchecked")
	public void add(Iterator<T>... it) {
		iterators.addAll(Arrays.asList(it));
	}
	
	@SuppressWarnings("unchecked")
	public void add(T... ts) {
		iterators.add(Arrays.asList(ts).iterator());
	}
}
