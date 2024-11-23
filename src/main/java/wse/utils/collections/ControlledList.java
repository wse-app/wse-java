package wse.utils.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ControlledList<E> extends ControlledCollection<E> implements List<E> {

	protected List<E> internal;
	private CollectionController<E> controller;

	public ControlledList(List<E> internal, CollectionController<E> controller) {
		super(internal, controller);
		this.internal = internal;
		this.controller = controller;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if (internal.addAll(c)) {
			for (E e : c) {
				controller.onEntryAdded(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (internal.addAll(c)) {
			for (E e : c) {
				controller.onEntryAdded(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public E get(int index) {
		return internal.get(index);
	}

	@Override
	public E set(int index, E element) {
		E e = get(index);
		controller.onEntryRemoved(e);
		controller.onEntryAdded(element);
		internal.set(index, element);
		return e;
	}

	@Override
	public void add(int index, E element) {
		internal.add(index, element);
		controller.onEntryAdded(element);
	}

	@Override
	public E remove(int index) {
		E e = internal.remove(index);
		controller.onEntryRemoved(e);
		return e;
	}

	@Override
	public int indexOf(Object o) {
		return internal.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return internal.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ControlledListIterator<>(internal.listIterator(), controller);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ControlledListIterator<>(internal.listIterator(index), controller);
	}

	/**
	 * The list returned is unmodifiable
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return Collections.unmodifiableList(internal.subList(fromIndex, toIndex));
	}

}
