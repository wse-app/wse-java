package wse.utils.options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.utils.Supplier;
import wse.utils.Suppliers;

public class Options implements IOptions {

	private Map<Option<?>, Object> options = new HashMap<>();

	public <T> void set(Option<T> option, T value) {
		options.put(option, value);
	}

	public <T> T get(Option<T> option) {
		return get(option, (Supplier<T>) null);
	}

	public <T> T get(Option<T> option, T def) {
		return get(option, Suppliers.ofInstance(def));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Option<T> option, Supplier<T> def) {
		if (option == null)
			return null;
		Object o = options.get(option);
		if (o == null) {
			if (def != null)
				return def.get();
			return option.getDefaultValue();
		}
		return (T) o;
	}

	@Override
	public void setOptions(HasOptions other) {
		setOptions(other.getOptions());
	}

	@Override
	public void setOptions(IOptions other) {
		this.options.putAll(other.getAll());
	}

	@Override
	public Map<Option<?>, Object> getAll() {
		return Collections.unmodifiableMap(options);
	}

	@Override
	public IOptions getOptions() {
		return this;
	}

	@Override
	public String toString() {
		return options.toString();
	}

	public void log(Logger log, Level level) {
		log(log, level, "Options");
	}

	public void log(Logger log, Level level, String title) {
		log.log(level, String.format("%s: %d", title, options.size()));

		for (Entry<Option<?>, Object> e : options.entrySet()) {
			log.log(level, String.format("%s: %s", String.valueOf(e.getKey()), String.valueOf(e.getValue())));
		}
	}

	public static final IOptions EMPTY = new IOptions() {

		@Override
		public void setOptions(HasOptions other) {

		}

		@Override
		public IOptions getOptions() {
			return this;
		}

		@Override
		public void setOptions(IOptions other) {
		}

		@Override
		public <T> void set(Option<T> option, T value) {
		}

		@Override
		public Map<Option<?>, Object> getAll() {
			return Collections.emptyMap();
		}

		@Override
		public <T> T get(Option<T> option, T def) {
			return def != null ? def : option != null ? option.getDefaultValue() : null;
		}

		@Override
		public <T> T get(Option<T> option) {
			return option != null ? option.getDefaultValue() : null;
		}
	};

}
