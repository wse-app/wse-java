package wse.utils.ini;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import wse.utils.internal.ILeaf;
import wse.utils.internal.PrettyPrinter;
import wse.utils.internal.StringGatherer;

public class IniFile extends IniSection implements ILeaf, PrettyPrinter {

	private static final long serialVersionUID = -972983931703326202L;

	private final Map<String, IniSection> sections;

	public IniFile() {
		sections = new LinkedHashMap<>();
	}

	@Override
	public void prettyPrint(StringGatherer builder, int level) {
		super.prettyPrint(builder, level);

		for (Map.Entry<String, IniSection> s : sections.entrySet()) {
			builder.add("[");
			builder.add(s.getKey());
			builder.add("]\r\n");
			s.getValue().setOptions(this);
			s.getValue().prettyPrint(builder, level);
		}

	}

	@Override
	public String getAttributeValue(String key, String namespace) {
		return getValue(key, namespace);
	}

	@Override
	public String getValue(String key, String namespace) {
		IniSection section = getSection(namespace);
		if (section == null)
			return null;
		return section.getValue(key);
	}

	@Override
	public Collection<String> getValueArray(String key, String namespace) {
		IniSection section = getSection(namespace);
		if (section == null)
			return Collections.emptyList();
		return section.getValueArray(key);
	}

	@Override
	public void setAttributeValue(String key, String namespace, Object value) {
		setValue(key, namespace, value);
	}

	@Override
	public void setAttributeValueArray(String key, String namespace, Iterable<Object> value) {
		setValueArray(key, namespace, value);
	}

	@Override
	public void setValue(String key, String namespace, Object value) {
		getSection(namespace, true).setValue(key, value);
	}

	@Override
	public void setValueArray(String key, String namespace, Iterable<Object> value) {
		getSection(namespace, true).setValueArray(key, value);
	}

	@Override
	public Collection<String> getAttributeValueArray(String key, String namespace) {
		return getValueArray(key, namespace);
	}

	public IniSection getGlobalSection() {
		return this;
	}

	public IniSection getSection(String name) {
		if (name == null)
			return getGlobalSection();
		return getSection(name, false);
	}

	private IniSection getSection(String name, boolean createIfNull) {
		IniSection section = sections.get(name);
		if (createIfNull && section == null) {
			section = new IniSection();
			sections.put(name, section);
		}
		return section;
	}

	public IniSection addSection(String name) {
		IniSection section = new IniSection();
		sections.put(name, section);
		return section;
	}

}
