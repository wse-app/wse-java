package wse.utils.collections;

public interface MapController<K, V> {
	void onEntryPut(K key, V value);

	void onEntryRemoved(K key, V value);
}
