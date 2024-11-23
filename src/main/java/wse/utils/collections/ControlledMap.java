package wse.utils.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ControlledMap<K, V> implements Map<K, V> {

	private MapController<K, V> controller;
	private Map<K, V> internal;

	public ControlledMap(Map<K, V> internal, MapController<K, V> controller) {
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
	public boolean containsKey(Object key) {
		return internal.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return internal.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return internal.get(key);
	}

	@Override
	public void clear() {
		for (Entry<K, V> e : internal.entrySet()) {
			controller.onEntryRemoved(e.getKey(), e.getValue());
		}
		internal.clear();
	}

	@Override
	public V put(K key, V value) {
		if (internal.containsKey(key)) {
			controller.onEntryRemoved(key, get(key));
		}
		controller.onEntryPut(key, value);
		return internal.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		if (internal.containsKey(key)) {
			K k = null;
			try {
				k = (K) key;
			} catch (ClassCastException e1) {
			}
			V v;
			controller.onEntryRemoved(k, v = internal.remove(key));
			return v;
		}
		return internal.remove(key);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new ControlledSet<Map.Entry<K, V>>(internal.entrySet(), new CollectionController<Map.Entry<K, V>>() {
			@Override
			public void onEntryAdded(Map.Entry<K, V> entry) {
				controller.onEntryPut(entry.getKey(), entry.getValue());
			}

			@Override
			public void onEntryRemoved(Map.Entry<K, V> removed) {
				controller.onEntryRemoved(removed.getKey(), removed.getValue());
			}
		});
	}

	@Override
	public Set<K> keySet() {
		return new ControlledSet<K>(internal.keySet(), new CollectionController<K>() {
			@Override
			public void onEntryAdded(K key) {
				controller.onEntryPut(key, get(key));
			}

			@Override
			public void onEntryRemoved(K removed) {
				controller.onEntryRemoved(removed, get(removed));
			}
		});
	}

	@Override
	public Collection<V> values() {
		return new ControlledCollection<V>(internal.values(), new CollectionController<V>() {
			@Override
			public void onEntryAdded(V entry) {
				controller.onEntryPut(null, entry);
			}

			@Override
			public void onEntryRemoved(V removed) {
				controller.onEntryRemoved(null, removed);
			}
		});
	}

}
