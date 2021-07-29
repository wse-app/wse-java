package wse.utils;

import java.util.Map;

public interface IOptions extends HasOptions {
	<T> void set(Option<T> option, T value);
	<T> T get(Option<T> option);
	<T> T get(Option<T> option, T def);
	
	Map<Option<?>, Object> getAll();
	void setOptions(IOptions other);
}
