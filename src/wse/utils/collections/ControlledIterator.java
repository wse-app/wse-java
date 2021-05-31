package wse.utils.collections;

import java.util.Iterator;

public class ControlledIterator<E> implements Iterator<E> {

	private CollectionController<E> controller;
	private Iterator<E> internal;
	
	protected E current;
	
	public ControlledIterator(Iterator<E> internal, CollectionController<E> controller) {
		this.internal = internal;
		this.controller = controller;
	}
	
	@Override
	public boolean hasNext() {
		return internal.hasNext();
	}

	@Override
	public E next() {
		return current = internal.next();
	}

	@Override
	public void remove() {
		internal.remove();
		controller.onEntryRemoved(current);
		current = null;
	}
}
