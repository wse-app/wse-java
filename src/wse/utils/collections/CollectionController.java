package wse.utils.collections;

public interface CollectionController<E> {
	void onEntryAdded(E entry);

	void onEntryRemoved(E removed);
}
