package wse.utils.collections;

import java.util.Collection;
import java.util.Iterator;

public class ControlledCollection<E> implements Collection<E>{

	private CollectionController<E> controller;
	protected Collection<E> internal;

	public ControlledCollection(Collection<E> internal, CollectionController<E> controller) {
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
		return new ControlledIterator<>(internal.iterator(), controller);
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
		if (internal.remove(o)) {
			E e = null;
			try {
				e = (E) o;
			} catch(ClassCastException e1) {}
			controller.onEntryRemoved(e);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internal.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean any = false;
		for (E e : c) {
			any = add(e) || any;
		}
		return any;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean any = false;
		for (Object e : c) {
			any = remove(e) || any;
		}
		return any;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean any = false;
		for (E e : internal) {
			if (!c.contains(e)) {
				any = remove(e) || any;
			}
		}
		return any;
	}

	@Override
	public void clear() {
		for (E e : internal) {
			controller.onEntryRemoved(e);
		}
		internal.clear();
	}
	
	
	

}
