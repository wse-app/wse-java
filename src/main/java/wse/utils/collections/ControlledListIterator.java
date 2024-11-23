package wse.utils.collections;

import java.util.ListIterator;

public class ControlledListIterator<E> extends ControlledIterator<E> implements ListIterator<E> {

	private ListIterator<E> internal;
	private CollectionController<E> controller;

	public ControlledListIterator(ListIterator<E> internal, CollectionController<E> controller) {
		super(internal, controller);
		this.internal = internal;
		this.controller = controller;
	}

	@Override
	public boolean hasPrevious() {
		return internal.hasPrevious();
	}

	@Override
	public E previous() {
		return current = internal.previous();
	}

	@Override
	public int nextIndex() {
		return internal.nextIndex();
	}

	@Override
	public int previousIndex() {
		return internal.previousIndex();
	}

	@Override
	public void set(E e) {
		E p = current;
		internal.set(e);
		controller.onEntryRemoved(p);
		controller.onEntryAdded(e);
	}

	@Override
	public void add(E e) {
		internal.add(e);
		controller.onEntryAdded(e);
	}

}
