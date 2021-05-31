package wse.utils.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import wse.utils.writable.StreamCatcher;

public class XMLElement extends XMLNode {

	public byte[] value;
	protected boolean cdata;

	protected final XChildList children = new XChildList();
	protected final XAttributeList attributes = new XAttributeList();
	
	protected LocationData parsedLocation;

	public XMLElement() {
		this(null);
	}

	public XMLElement(String name) {
		super(name);
	}

	public XMLElement(String name, String namespace) {
		super(name, namespace);
	}

	public XMLElement(String prefix, String name, String namespace) {
		super(prefix, name, namespace);
	}
	
	
	public LocationData getLocationData() {
		return parsedLocation;
	}

	/** returns this xml elements index in its parent's element list or -1 if this element has no parent */
	public int index() {
		if (parent != null)
			return -1;
		return parent.getChildren().indexOf(this);
	}
	
	public String getValue() {
		return new String(value, tree.getCharset());
	}

	public byte[] getRawValue() {
		return value;
	}

	public XMLElement setValue(String value) {
		return setValue(value, false);
	}
	
	public <T> XMLElement setValue(T value) {
		return setValue(String.valueOf(value), false);
	}
	
	public <T> XMLElement setValue(T value, boolean cdata) {
		return setValue(String.valueOf(value), cdata);
	}

	public XMLElement setValue(String value, boolean cdata) {
		if (cdata) {
			this.value = value.getBytes();
		} else {
			this.value = value.getBytes(tree.getCharset());
		}
		return this;
	}

	public XMLElement setValueRaw(byte[] value) {
		return setValueRaw(value, false);
	}

	public XMLElement setValueRaw(byte[] value, boolean cdata) {
		this.value = value;
		this.cdata = cdata;
		return this;
	}

	public XMLElement writeAsCDATA(boolean cdata) {
		this.cdata = cdata;
		return this;
	}

	public boolean hasCDATA() {
		return cdata;
	}

	public void propagateTree(XMLTree tree) {
		super.propagateTree(tree);
		for (XMLNode n : children)
			n.propagateTree(tree);
		for (XMLNode n : attributes)
			n.propagateTree(tree);
	}

	public XChildList getChildren() {
		return children;
	}

	public XAttributeList getAttributes() {
		return attributes;
	}

	public XMLElement getChild(String name) {
		return children.getFirst(name);
	}

	public XMLElement getChild(String name, String namespace) {
		return children.getFirst(name, namespace);
	}

	public List<XMLElement> getChildren(String name) {
		return children.getAll(name);
	}

	public List<XMLElement> getChildren(String name, String namespace) {
		return children.getAll(name, namespace);
	}

	public XMLAttribute getAttribute(String name) {
		return attributes.getFirst(name);
	}
	
	public List<XMLAttribute> getNamespaceDeclarations() {
		return getAttributesNS(XMLUtils.XMLNS);
	}
	
	public List<XMLAttribute> getAttributesNS(String namespace) {
		return attributes.getAllNS(namespace);
	}

	public List<XMLAttribute> getAttributes(String name) {
		return attributes.getAll(name);
	}

	public XMLAttribute getAttribute(String name, String namespace) {
		return attributes.getFirst(name, namespace);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public boolean hasValue() {
		return value != null && value.length != 0;
	}

	public boolean hasAttributes() {
		return attributes.size() != 0;
	}

	public byte[] toByteArray() {
		return StreamCatcher.from(this).toByteArray();
	}
	
	public String toString() {
		return new String(toByteArray(), tree.getCharset());
	}

	public void writeToStream(OutputStream stream) throws IOException {
		tree.write(stream, 0);
		write(stream, 0);
	}

	public void write(OutputStream stream, int level) throws IOException {
		byte[] tabs = XMLUtils.level(level);
		stream.write(tabs);
		stream.write('<');
		byte[] qname = getQualifiedName().getBytes(tree.getCharset());
		stream.write(qname);

		if (hasAttributes()) {
			Iterator<XMLAttribute> it = attributes.iterator();
			int len = 0;

			XMLAttribute att;
			while (it.hasNext() && len < 30 && (att = it.next()) != null) {
				stream.write(' ');
				att.write(stream, 0);
				len += att.length();
			}

			while (it.hasNext() && (att = it.next()) != null) {
				stream.write('\n');
				att.write(stream, level + 2);
			}
		}

		if (hasChildren()) {
			stream.write('>');
			stream.write('\n');

			for (XMLElement child : children) {
				child.write(stream, level + 1);
			}

			stream.write(tabs);
			stream.write('<');
			stream.write('/');
			stream.write(qname);
		} else if (hasValue()) {
			stream.write('>');

			if (cdata) {
				stream.write(XMLUtils.convertToCDATA(value));
			} else {
				stream.write(XMLUtils.escape(getValue()).getBytes(tree.getCharset()));
			}
			stream.write('<');
			stream.write('/');
			stream.write(qname);
		} else {
			stream.write('/');
		}
		stream.write('>');
		stream.write('\n');
	}

	public XMLElement addChild(XMLElement node) {
		children.add(node);
		return node;
	}

	public XMLElement addChild(String name) {
		return addChild(new XMLElement(name));
	}

	public XMLElement addChild(String name, String namespace) {
		return addChild(new XMLElement(name, namespace));
	}

	public <T> XMLElement addChildValue(String name, T value) {
		return addChildValue(name, null, value);
	}

	public <T> XMLElement addChildValue(String name, String namespace, T value) {
		return addChild(new XMLElement(name, namespace).setValue(String.valueOf(value)));
	}

	public <T> void addChildValues(String name, T[] values) {
		for (T value : values)
			addChildValue(name, value);
	}

	public <T> void addChildValues(String name, String namespace, T[] values) {
		for (T value : values)
			addChildValue(name, namespace, value);
	}

	public <T> void addChildValues(String name, Iterable<T> values) {
		for (T value : values)
			addChildValue(name, value);
	}

	public <T> void addChildValues(String name, String namespace, Iterable<T> values) {
		for (T value : values)
			addChildValue(name, namespace, value);
	}

	public String getChildValue(String name) {
		XMLElement child = getChild(name);
		if (child != null)
			return child.getValue();
		return null;
	}
	
	public String getChildValue(String name, String namespace) {
		XMLElement child = getChild(name, namespace);
		if (child != null)
			return child.getValue();
		return null;
	}
	
	public String getChildValueDef(String name, String defValue) {
		String result = getChildValue(name);
		return result == null ? defValue : result;
	}
	
	public String getChildValueDef(String name, String namespace, String defValue) {
		String result = getChildValue(name, namespace);
		return result == null ? defValue : result;
	}
	
	public Iterable<String> getChildValues(String name) {
		return XMLUtils.values(getChildren(name));
	}

	public Iterable<String> getChildValues(String name, String namespace) {
		return XMLUtils.values(getChildren(name, namespace));
	}

	public XMLAttribute addAttribute(XMLAttribute attribute) {
		attributes.add(attribute);
		return attribute;
	}

	public XMLAttribute addAttribute(String name, String value) {
		return addAttribute(new XMLAttribute(name, value));
	}

	public XMLAttribute addAttribute(String name, String value, String namespace) {
		return addAttribute(new XMLAttribute(name, value, namespace));
	}

	public String getAttributeValue(String name) {
		XMLAttribute a = getAttribute(name);
		if (a != null) return a.getValue();
		return null;
	}
	
	public String getAttributeValueDef(String name, String defaultValue) {
		XMLAttribute a = getAttribute(name);
		if (a != null) return a.getValue();
		return defaultValue;
	}
	
	public String getAttributeValue(String name, String namespace) {
		XMLAttribute a = getAttribute(name, namespace);
		if (a != null) return a.getValue();
		return null;
	}
	
	public String getAttributeValueDef(String name, String namespace, String defaultValue) {
		XMLAttribute a = getAttribute(name, namespace);
		if (a != null) return a.getValue();
		return defaultValue;
	}
	
	public XMLAttribute declareNamespace(String name, String namespace) {
		return addAttribute(XMLAttribute.namespace(name, namespace));
	}

	public XMLElement getChildOfNames(String namespace, String... names) {
		for (XMLElement c : getChildren()) {
			if (Objects.equals(namespace, c.getNamespaceURI())) {
				for (String name : names)
					if (Objects.equals(name, c.getName()))
						return c;
			}
		}
		return null;
	}
	
	public List<XMLElement> getChildrenOfNames(String namespace, String... names) {
		List<XMLElement> result = new LinkedList<>();
		for (XMLElement c : getChildren()) {
			if (Objects.equals(namespace, c.getNamespaceURI())) {
				for (String name : names)
					if (Objects.equals(name, c.getName()))
						result.add(c);
			}
		}
		return result;
	}
	
	public XMLElement getFirstParent(String name) {
		if (Objects.equals(this.name, name))
			return this;
		if (parent != null)
			return parent.getFirstParent(name);
		return null;
	}
	
	public XMLElement getFirstParent(String name, String namespace) {
		if (Objects.equals(this.name, name) && Objects.equals(this.namespace, namespace))
			return this;
		if (parent != null)
			return parent.getFirstParent(name, namespace);
		return null;
	}
	
	public String findDefaultNamespace() {
		return findNamespaceURI(null);
	}
	
	public String findNamespaceURI(String prefix) {
		for (XMLAttribute attrib : this.attributes) {
			if (attrib.isNamespaceDeclaration()
					&& ((prefix == null && attrib.isDefaultNamespace()) || Objects.equals(prefix, attrib.getName()))) {
				return attrib.getValue();
			}
		}
		if (parent != null)
			return parent.findNamespaceURI(prefix);
		return null;
	}

	public String findPrefix(String namespaceURI) {
		for (XMLAttribute attrib : this.attributes) {
			if (attrib.isNamespaceDeclaration() && Objects.equals(namespaceURI, attrib.getValue())) {
				if ("xmlns".equals(attrib.name))
					return "";
				else
					return attrib.getName();
			}
		}

		if (parent != null)
			return parent.findPrefix(namespaceURI);
		return null;
	}
	
	public class XChildList extends XMLNodeList<XMLElement> {
		protected XChildList() {
			super(XMLElement.this);
		}
	}

	public class XAttributeList extends XMLNamedNodeMap<XMLAttribute> {
		protected XAttributeList() {
			super(XMLElement.this);
		}
	}

	public static XMLElement parseNode(Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE)
			throw new IllegalStateException(
					"Could not convert Node of type " + XMLUtils.nodeTypeStr(node.getNodeType()) + " to Element");

		String[] nodeName = node.getNodeName().split(":", 2);

		XMLElement res = new XMLElement();
		res.prefix = node.getPrefix();
		res.namespace = node.getNamespaceURI();
		res.name = nodeName[nodeName.length - 1];
		res.parsedLocation = (LocationData) node.getUserData(LocationData.LOCATION_DATA_KEY);
		
		boolean cdata = false;

		for (Node e = node.getFirstChild(); e != null; e = e.getNextSibling()) {
			if (e.getNodeType() == Node.ELEMENT_NODE)
				res.addChild(parseNode(e));
			else if (e.getNodeType() == Node.CDATA_SECTION_NODE) {
				cdata = true;
			}
		}

		if (!res.hasChildren()) {
			res.setValue(node.getTextContent(), cdata);
		}

		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			res.addAttribute(XMLAttribute.parseNode(attributes.item(i)));
		}

		return res;
	}

	public void resetHierarchyNamespaces() {
		XMLUtils.resetNamespaces(this);
	}
}
