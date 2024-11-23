package wse.utils.collections;

import java.util.HashMap;
import java.util.Map;

public class Occurances<T> extends HashMap<T, Integer> {
	private static final long serialVersionUID = 4828974450252417738L;

	public void add(T key) {
		super.put(key, get(key) + 1);
	}

	public Integer get(Object key) {
		Integer r;
		if ((r = super.get(key)) == null)
			return 0;
		return r;
	}

	public T getMostCommon() {
		int v = 0;
		T k = null;
		Integer v_;
		for (Map.Entry<T, Integer> o : this.entrySet()) {
			if ((v_ = o.getValue()) != null && v_ > v) {
				v = v_;
				k = o.getKey();
			}
		}
		return k;
	}

}
