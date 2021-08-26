package wse.utils.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collection;

import wse.WSE;
import wse.utils.MimeType;
import wse.utils.exception.WseParsingException;
import wse.utils.http.StreamUtils;
import wse.utils.internal.IElement;
import wse.utils.internal.ILeaf;
import wse.utils.internal.InternalFormat;
import wse.utils.internal.StringGatherer;
import wse.utils.options.HasOptions;
import wse.utils.options.IOptions;
import wse.utils.options.Option;
import wse.utils.options.Options;

public class Config implements ILeaf, HasOptions {

	public static final Option<InvalidValueRule> INVALID_VALUE_RULE = new Option<>(Config.class, "INVALID_VALUE_RULE",
			InvalidValueRule.EXCEPTION);

	public static enum InvalidValueRule {
		DEFAULT_VALUE, EXCEPTION
	}

	private ILeaf leaf;
	private Options options;

	public static boolean saveDefault(File target, InputStream source) throws IOException {
		if (target.exists())
			return false;

		if (!target.createNewFile())
			throw new IOException("Failed to create file");

		try (FileOutputStream out = new FileOutputStream(target, false)) {
			StreamUtils.write(source, out, 1024);
			source.close();
		}

		return true;
	}

	public Config(MimeType mt) {
		this(InternalFormat.createEmpty(mt));

		if (this.leaf instanceof IElement) {
			((IElement) this.leaf).setName("Config");
		}
	}

	public Config(ILeaf leaf) {
		if (leaf == null)
			throw new NullPointerException("leaf == null");

		this.leaf = leaf;
		this.options = new Options();
	}

	@Override
	public IOptions getOptions() {
		return options;
	}

	@Override
	public void setOptions(HasOptions other) {
		options.setOptions(other);
	}

	@Override
	public int getRow() {
		return leaf.getRow();
	}

	@Override
	public int getColumn() {
		return leaf.getColumn();
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		leaf.writeToStream(stream, charset);
	}

	@Override
	public StringGatherer prettyPrint() {
		return leaf.prettyPrint();
	}

	@Override
	public StringGatherer prettyPrint(int level) {
		return leaf.prettyPrint(level);
	}

	@Override
	public void prettyPrint(StringGatherer builder, int level) {
		leaf.prettyPrint();
	}

	@Override
	public MimeType getMimeType() {
		return leaf.getMimeType();
	}

	@Override
	public Charset preferredCharset() {
		return leaf.preferredCharset();
	}

	@Override
	public String getValue(String key) {
		return leaf.getValue(key);
	}

	@Override
	public String getValue(String key, String namespace) {
		return leaf.getValue(key, namespace);
	}

	@Override
	public void setValue(String key, Object value) {
		leaf.setValue(key, value);
	}

	@Override
	public void setValue(String key, String namespace, Object value) {
		leaf.setValue(key, namespace, value);
	}

	@Override
	public Collection<String> getValueArray(String key) {
		return leaf.getValueArray(key);
	}

	@Override
	public Collection<String> getValueArray(String key, String namespace) {
		return leaf.getValueArray(key, namespace);
	}

	@Override
	public void setValueArray(String key, Iterable<Object> value) {
		leaf.setValueArray(key, value);
	}

	@Override
	public void setValueArray(String key, String namespace, Iterable<Object> value) {
		leaf.setValueArray(key, namespace, value);
	}

	@Override
	public String getAttributeValue(String key) {
		return leaf.getAttributeValue(key);
	}

	@Override
	public String getAttributeValue(String key, String namespace) {
		return leaf.getAttributeValue(key, namespace);
	}

	@Override
	public void setAttributeValue(String key, Object value) {
		leaf.setAttributeValue(key, value);
	}

	@Override
	public void setAttributeValue(String key, String namespace, Object value) {
		leaf.setAttributeValue(key, namespace, value);
	}

	@Override
	public Collection<String> getAttributeValueArray(String key) {
		return leaf.getAttributeValueArray(key);
	}

	@Override
	public Collection<String> getAttributeValueArray(String key, String namespace) {
		return leaf.getAttributeValueArray(key, namespace);
	}

	@Override
	public void setAttributeValueArray(String key, Iterable<Object> value) {
		leaf.setAttributeValueArray(key, value);
	}

	@Override
	public void setAttributeValueArray(String key, String namespace, Iterable<Object> value) {
		leaf.setAttributeValueArray(key, namespace, value);
	}

	public String getString(String key) {
		return getString(key, null, null);
	}

	public String getString(String key, String namespace) {
		return getString(key, namespace, null);
	}

	public String getString(String key, String namespace, String def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;
		return sValue;
	}

	public Byte getByte(String key) {
		return getByte(key, null, null);
	}

	public Byte getByte(String key, Byte def) {
		return getByte(key, null, def);
	}

	public Byte getByte(String key, String namespace) {
		return getByte(key, namespace, null);
	}

	public Byte getByte(String key, String namespace, Byte def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return Byte.parseByte(sValue);
		} catch (Exception e) {
			return invalidValueRule(Byte.class, def, sValue, e);
		}
	}

	public Integer getInt(String key) {
		return getInt(key, null, null);
	}

	public Integer getInt(String key, Integer def) {
		return getInt(key, null, def);
	}

	public Integer getInt(String key, String namespace) {
		return getInt(key, namespace, null);
	}

	public Integer getInt(String key, String namespace, Integer def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return Integer.parseInt(sValue);
		} catch (Exception e) {
			return invalidValueRule(Integer.class, def, sValue, e);
		}
	}

	public Short getShort(String key) {
		return getShort(key, null, null);
	}

	public Short getShort(String key, Short def) {
		return getShort(key, null, def);
	}

	public Short getShort(String key, String namespace) {
		return getShort(key, namespace, null);
	}

	public Short getShort(String key, String namespace, Short def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return Short.parseShort(sValue);
		} catch (Exception e) {
			return invalidValueRule(Short.class, def, sValue, e);
		}
	}

	public Long getLong(String key) {
		return getLong(key, null, null);
	}

	public Long getLong(String key, Long def) {
		return getLong(key, null, def);
	}

	public Long getLong(String key, String namespace) {
		return getLong(key, namespace, null);
	}

	public Long getLong(String key, String namespace, Long def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return Long.parseLong(sValue);
		} catch (Exception e) {
			return invalidValueRule(Long.class, def, sValue, e);
		}
	}

	public BigInteger getBigInt(String key) {
		return getBigInt(key, null, null);
	}

	public BigInteger getBigInt(String key, BigInteger def) {
		return getBigInt(key, null, def);
	}

	public BigInteger getBigInt(String key, String namespace) {
		return getBigInt(key, namespace, null);
	}

	public BigInteger getBigInt(String key, String namespace, BigInteger def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return new BigInteger(sValue);
		} catch (Exception e) {
			return invalidValueRule(BigInteger.class, def, sValue, e);
		}
	}

	public Float getFloat(String key) {
		return getFloat(key, null, null);
	}

	public Float getFloat(String key, Float def) {
		return getFloat(key, null, def);
	}

	public Float getFloat(String key, String namespace) {
		return getFloat(key, namespace, null);
	}

	public Float getFloat(String key, String namespace, Float def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return Float.parseFloat(sValue);
		} catch (Exception e) {
			return invalidValueRule(Float.class, def, sValue, e);
		}
	}

	public Double getDouble(String key) {
		return getDouble(key, null, null);
	}

	public Double getDouble(String key, Double def) {
		return getDouble(key, null, def);
	}

	public Double getDouble(String key, String namespace) {
		return getDouble(key, namespace, null);
	}

	public Double getDouble(String key, String namespace, Double def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return Double.parseDouble(sValue);
		} catch (Exception e) {
			return invalidValueRule(Double.class, def, sValue, e);
		}
	}

	public BigDecimal getBigDecimal(String key) {
		return getBigDecimal(key, null, null);
	}

	public BigDecimal getBigDecimal(String key, BigDecimal def) {
		return getBigDecimal(key, null, def);
	}

	public BigDecimal getBigDecimal(String key, String namespace) {
		return getBigDecimal(key, namespace, null);
	}

	public BigDecimal getBigDecimal(String key, String namespace, BigDecimal def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		try {
			return new BigDecimal(sValue);
		} catch (Exception e) {
			return invalidValueRule(BigDecimal.class, def, sValue, e);
		}
	}

	public Character getChar(String key) {
		return getChar(key, null, null);
	}

	public Character getChar(String key, Character def) {
		return getChar(key, null, def);
	}

	public Character getChar(String key, String namespace) {
		return getChar(key, namespace, null);
	}

	public Character getChar(String key, String namespace, Character def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		if (sValue.length() != 1)
			return invalidValueRule(Character.class, def, sValue, null);

		return sValue.charAt(0);

	}

	public Boolean getBool(String key) {
		return getBool(key, null, null);
	}

	public Boolean getBool(String key, Boolean def) {
		return getBool(key, null, def);
	}

	public Boolean getBool(String key, String namespace) {
		return getBool(key, namespace, null);
	}

	public Boolean getBool(String key, String namespace, Boolean def) {
		String sValue = getValue(key, namespace);
		if (sValue == null)
			return def;

		return WSE.parseBool(sValue);
	}

	private final <T> T invalidValueRule(Class<T> clazz, T def, String value, Throwable cause) {
		InvalidValueRule rule = getOptions().get(INVALID_VALUE_RULE);

		switch (rule) {
		case EXCEPTION:
			throw new WseParsingException("Not of type " + clazz.getName() + ": '" + value + "'", cause);
		case DEFAULT_VALUE:
		default:
			return def;
		}
	}

	@Override
	public String toString() {
		return leaf.toString();
	}

	@Override
	public void preparePrint() {
		leaf.preparePrint();
	}
}
