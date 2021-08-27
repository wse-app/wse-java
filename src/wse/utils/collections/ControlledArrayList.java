package wse.utils.collections;

import java.util.ArrayList;
import java.util.Collection;

public class ControlledArrayList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1462123578692416733L;

	private CollectionController<E> controller;

	public ControlledArrayList(CollectionController<E> controller) {
		super();
		this.controller = controller;
	}

	@Override
	public E remove(int index) {
		E e = super.remove(index);
		controller.onEntryRemoved(e);
		return e;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		boolean removed = super.remove(o);
		if (removed) {
			E e = null;
			try {
				e = (E) o;
			} catch (ClassCastException e1) {
			}
			controller.onEntryRemoved(e);
		}
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		batchControl(c, false);
		return super.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		batchControl(c, true);
		return super.retainAll(c);
	}

	private void batchControl(Collection<?> c, boolean complement) {
		int r = 0;
		E e;

		for (; r < size(); r++)
			if (c.contains(e = get(r)) != complement)
				controller.onEntryRemoved(e);

	}

	@Override
	public E set(int index, E element) {
		E e = get(index);
		controller.onEntryRemoved(e);
		controller.onEntryAdded(element);
		return super.set(index, element);
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		for (int i = fromIndex; i < toIndex; i++) {
			controller.onEntryRemoved(get(i));
		}
		super.removeRange(fromIndex, toIndex);
	}

	@Override
	public boolean add(E e) {
		controller.onEntryAdded(e);
		return super.add(e);
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		controller.onEntryAdded(element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean added = super.addAll(c);
		if (added)
			for (E e : c)
				controller.onEntryAdded(e);
		return added;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean added = super.addAll(index, c);
		if (added)
			for (E e : c)
				controller.onEntryAdded(e);
		return added;
	}
}
