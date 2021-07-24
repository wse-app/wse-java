package wse.utils.internal;

import java.nio.charset.Charset;
import java.util.Collection;

import wse.utils.MimeType;
import wse.utils.json.PrettyPrinter;
import wse.utils.writable.StreamWriter;

public interface IElement extends HasRowColumn, StreamWriter, PrettyPrinter {
	
	public IElement createEmpty();
	
	public String getValue(String key);
	public String getValue(String key, String namespace);
	
	public String getAttributeValue(String key);
	public String getAttributeValue(String key, String namespace);
	
	public IElement getChild(String key);
	public IElement getChild(String key, String namespace);
	
	public Collection<String> getValueArray(String key);
	public Collection<String> getValueArray(String key, String namespace);
	
	public <T extends IElement> Collection<T> getChildArray(String key);
	public <T extends IElement> Collection<T> getChildArray(String key, String namespace);
	
	public void setValue(String key, Object value);
	public void setValue(String key, String namespace, Object value);
	
	public void setValueArray(String key, Iterable<Object> value);
	public void setValueArray(String key, String namespace, Iterable<Object> value);
	
	public void setAttributeValue(String key, Object value);
	public void setAttributeValue(String key, String namespace, Object value);
	
	public void setAttributeValueArray(String key, Iterable<Object> value);
	public void setAttributeValueArray(String key, String namespace, Iterable<Object> value);
	
	public void setChild(String key, IElement child);
	public void setChild(String key, String namespace, IElement child);
	
	public void setChildArray(String key, Iterable<IElement> children);
	public void setChildArray(String key, String namespace, Iterable<IElement> children);
	
	public MimeType getMimeType();
	public Charset preferredCharset();
}
