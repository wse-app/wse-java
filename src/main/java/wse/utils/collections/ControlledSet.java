package wse.utils.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ControlledSet<E> implements Set<E> {

	private Set<E> internal;
	private CollectionController<E> controller;

	public ControlledSet(Set<E> internal, CollectionController<E> controller) {
		this.internal = internal;
		this.controller = controller;
	}

	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return internal.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return new ControlledIterator<E>(internal.iterator(), controller);
	}

	@Override
	public Object[] toArray() {
		return internal.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return internal.toArray(a);
	}

	@Override
	public boolean add(E e) {

		if (internal.add(e)) {
			controller.onEntryAdded(e);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if (internal.contains(o)) {
			E e = null;
			try {
				e = (E) o;
			} catch (ClassCastException e1) {
			}
			controller.onEntryRemoved(e);
			return internal.remove(o);
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internal.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) {
			if (!contains(e))
				controller.onEntryAdded(e);
		}
		return internal.addAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c) {
		for (Object o : this)
			if (!c.contains(o)) {
				E e = null;
				try {
					e = (E) o;
				} catch (ClassCastException e1) {
				}
				controller.onEntryRemoved(e);
			}
		return internal.retainAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o)) {
				E e = null;
				try {
					e = (E) o;
				} catch (ClassCastException e1) {
				}
				controller.onEntryRemoved(e);
			}
		return internal.removeAll(c);
	}

	@Override
	public void clear() {
		for (E e : internal) {
			controller.onEntryRemoved(e);
		}
		internal.clear();
	}

}
