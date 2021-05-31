package wse.utils.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControlledHashMap<K, V> extends HashMap<K, V>{
	private static final long serialVersionUID = -8719348963132427008L;

	private MapController<K, V> controller;
	public ControlledHashMap(MapController<K, V> controller) {
		this.controller = controller;
	}
	
	@Override
	public V put(K key, V value) {
		if (super.containsKey(key)) {
			controller.onEntryRemoved(key, get(key));
		}
		controller.onEntryPut(key, value);
		return super.put(key, value);
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
		if (super.containsKey(key)) {
			K k = null;
			try {
				k = (K) key;
			} catch (ClassCastException e1) {
			}
			V v;
			controller.onEntryRemoved(k, v = super.remove(key));
			return v;
		}
		return super.remove(key);
	}
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new ControlledSet<Map.Entry<K, V>>(super.entrySet(), new CollectionController<Map.Entry<K, V>>() {
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
		return new ControlledSet<K>(super.keySet(), new CollectionController<K>() {
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
		return new ControlledCollection<V>(super.values(), new CollectionController<V>() {
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
