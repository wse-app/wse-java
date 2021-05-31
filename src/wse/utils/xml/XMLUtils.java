package wse.utils.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import wse.utils.collections.Occurances;
import wse.utils.writable.StreamCatcher;
import wse.utils.writable.StreamWriter;
import wse.utils.xml.XMLHierarchyIterator.Receiver;

public class XMLUtils {
	
	public final static String XMLNS = "http://www.w3.org/2000/xmlns/";
	public final static String SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public final static String SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
	public final static String SOAP_ENVELOPE_12 = "http://www.w3.org/2003/05/soap-envelope/";
	public final static String SOAP_ENCODING = "http://www.w3.org/2003/05/soap-encoding";
	public final static String WSDL = "http://schemas.xmlsoap.org/wsdl/";
	public final static String SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
	public final static String SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
	public static final String HTTP = "http://schemas.xmlsoap.org/wsdl/http/";

	private static Map<String, String> defaultNSPrefix = new HashMap<>();
	static {
		defaultNSPrefix.put(XMLNS, "xmlns");
		defaultNSPrefix.put(SCHEMA, "xs");
		defaultNSPrefix.put(SOAP_ENVELOPE, "env");
		defaultNSPrefix.put(SOAP_ENCODING, "enc");
		defaultNSPrefix.put(WSDL, "wsdl");
		defaultNSPrefix.put(SOAP, "soap");
		defaultNSPrefix.put(SOAP12, "soap12");
		defaultNSPrefix.put(HTTP, "http");
	}

	
	
	final static String LINE_NUMBER_KEY_NAME = "lineNumber";
	final static String COLUMN_NUMBER_KEY_NAME = "columnNumber";

	private final static DocumentBuilderFactory documentBuilderFactory;
	private final static TransformerFactory transformerFactory;
	private final static Transformer nullTransformer;
	
	static {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = transformerFactory.newTransformer();
			t.setErrorListener(new EmptyErrorListener());
		} catch (TransformerConfigurationException e) {
			System.err.println("Failed to initialize xmltansformer");
		}
		nullTransformer = t;
	}
	
	public static Document parseDOMsimple(InputStream is)
			throws ParserConfigurationException, SAXException, IOException {
		Document result;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);

		result = doc;

		return result;
	}

	/** @throws ClassNotFoundException Android may not be able to use some features used by this parse method, use instead parseDOMsimple() if this is the case */
	public static Document parseDOMLineNRAware(final InputStream is)
			throws IOException, SAXException, ParserConfigurationException, TransformerException, NoClassDefFoundError, ClassNotFoundException {
		
		if (nullTransformer == null)
			return parseDOMsimple(is);
		
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		DOMResult domResult = new DOMResult(doc);

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		// saxParserFactory.setValidating(true);
		SAXParser saxParser = saxParserFactory.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setErrorHandler(null);
		LocationAnnotator locationAnnotator = new LocationAnnotator(xmlReader, doc);

		InputSource inputSource = new InputSource(is);
		SAXSource saxSource = new SAXSource(locationAnnotator, inputSource);
		
		nullTransformer.transform(saxSource, domResult);

		return doc;
	}

	public static String printDOM(Document document) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			StringWriter sw = new StringWriter();
			tf.newTransformer().transform(new DOMSource(document), new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static XMLElement parseDOM(Document document) {
		Node root = null;
		for (Node n = document.getFirstChild(); n != null && root == null; n = n.getNextSibling())
			if (n.getNodeType() == Node.ELEMENT_NODE)
				root = n;

		if (root == null) {
			return null;
		}

		XMLElement result = XMLElement.parseNode(root);

		if (document.getXmlVersion() != null)
			result.tree.setVersion(document.getXmlVersion());
		if (document.getXmlEncoding() != null)
			result.tree.setEncoding(document.getXmlEncoding());
		result.tree.setStandalone(document.getXmlStandalone());

		return result;
	}


	public static XMLElement parse(InputStream input) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		return parseSimple(input);
	}
	
	public static XMLElement parseLineNRAware(InputStream input) throws NoClassDefFoundError, ClassNotFoundException, IOException, SAXException, ParserConfigurationException, TransformerException {
		return parseDOM(parseDOMLineNRAware(input));
	}
	
	public static XMLElement parseSimple(InputStream input) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		return parseDOM(parseDOMsimple(input));
	}

	public static Iterable<String> values(Iterable<XMLElement> elements) {
		final Iterator<XMLElement> children = elements.iterator();
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {

					@Override
					public void remove() {
						children.remove();
					}

					@Override
					public String next() {
						return children.next().getValue();
					}

					@Override
					public boolean hasNext() {
						return children.hasNext();
					}
				};
			}
		};
	}

	public static String escape(String value) {
		return value.replace((CharSequence) "&", "&amp;").replace((CharSequence) "\"", "&quot;")
				.replace((CharSequence) "'", "&apos;").replace((CharSequence) "<", "&lt;")
				.replace((CharSequence) ">", "&gt;");
	}

	public static byte[] convertToCDATA(byte[] value) {
		byte[] cdata = new byte[] { '<', '!', '[', 'C', 'D', 'A', 'T', 'A', '[' };
		byte[] cend = new byte[] { ']', ']', '>' };

		byte[] csplit = new byte[] { ']', ']', ']', ']', '>', '<', '!', '[', 'C', 'D', 'A', 'T', 'A', '[', '>' };

		int finalSize = 9 + value.length + 3;
		for (int i = 0; i < value.length - 2; i++) {
			if (value[i] == ']' && value[i + 1] == ']' && value[i + 2] == '>') {
				finalSize += 12;
				i += 2;
			}
		}

		byte[] result = new byte[finalSize];

		for (int i = 0; i < cdata.length; i++)
			result[i] = cdata[i];

		for (int i = 0; i < cend.length; i++)
			result[result.length - cend.length + i] = cend[i];

		result[result.length - 5] = value[value.length - 2];
		result[result.length - 4] = value[value.length - 1];

		for (int i = 9, j = 0; j < value.length - 2; i++, j++) {
			if (value[j] == ']' && value[j + 1] == ']' && value[j + 2] == '>') {
				for (int a = 0; a < csplit.length; a++)
					result[i++] = csplit[a];
				j += 2;
				i--;
			} else {
				result[i] = value[j];
			}
		}
		return result;
	}

	static Map<Short, String> type_str = new HashMap<>();
	static {
		type_str.put(Node.ELEMENT_NODE, "element");
		type_str.put(Node.ATTRIBUTE_NODE, "attribute");
		type_str.put(Node.TEXT_NODE, "text");
		type_str.put(Node.CDATA_SECTION_NODE, "cdata");
		type_str.put(Node.ENTITY_REFERENCE_NODE, "entity ref");
		type_str.put(Node.ENTITY_NODE, "entity");
		type_str.put(Node.PROCESSING_INSTRUCTION_NODE, "instruction");
		type_str.put(Node.COMMENT_NODE, "comment");
		type_str.put(Node.DOCUMENT_NODE, "document");
		type_str.put(Node.DOCUMENT_TYPE_NODE, "doc type");
		type_str.put(Node.DOCUMENT_FRAGMENT_NODE, "doc fragment");
		type_str.put(Node.NOTATION_NODE, "notation");
	}

	public static String nodeTypeStr(short type) {
		if (type_str.containsKey(type))
			return type_str.get(type);
		return type + "?";
	}

	public static void print(Node node, int level) {
		System.out.print(level_str(level));
		System.out.println(
				nodeTypeStr(node.getNodeType()) + "; '" + node.getNodeName() + "'; '" + trim(node.getTextContent()));

		NamedNodeMap att = node.getAttributes();
		if (att != null)
			for (int i = 0; i < att.getLength(); i++) {
				print(att.item(i), level + 1);
			}

		NodeList nl = node.getChildNodes();
		if (nl != null)
			for (int i = 0; i < nl.getLength(); i++) {
				print(nl.item(i), level + 1);
			}
	}

	private static String trim(String s) {
		if (s == null)
			return null;
		return s.trim();
	}

	private static final String[] levels_str = { "", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t\t",
			"\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t", };

	protected static String level_str(int level) {
		return levels_str[(level) % levels_str.length];
	}

	private static final byte[][] levels = { {}, { '\t' }, { '\t', '\t' }, { '\t', '\t', '\t' },
			{ '\t', '\t', '\t', '\t' }, { '\t', '\t', '\t', '\t', '\t' }, { '\t', '\t', '\t', '\t', '\t', '\t' },
			{ '\t', '\t', '\t', '\t', '\t', '\t', '\t' }, { '\t', '\t', '\t', '\t', '\t', '\t', '\t', '\t' },
			{ '\t', '\t', '\t', '\t', '\t', '\t', '\t', '\t', '\t' },
			{ '\t', '\t', '\t', '\t', '\t', '\t', '\t', '\t', '\t', '\t' } };

	protected static byte[] level(int i) {
		return levels[i];
	}

	
	protected static void solveNamespacePrefixes(XMLElement e) {
		solveNamespacePrefixes(e, new ArrayList<String[]>());
	}

	private static void solveNamespacePrefixes(XMLElement e, List<String[]> map) {
		int size = map.size();

		for (XMLAttribute a : e.getNamespaceDeclarations()) {
			map.add(a.ns());
		}

		if (e.namespace != null) {
			String prefix = findPrefix(map, e.namespace);
			if (prefix == null) {
//				System.out.println("WARNING: namespace \"" + e.namespace + "\" was never defined or was not accessable");
			} else {
				e.prefix = prefix.isEmpty() ? null : prefix;
			}
		}

		for (XMLAttribute a : e.attributes) {
			if (!a.isNamespaceDeclaration())
				solveNamespacePrefixes(a, map);
		}

		for (XMLElement c : e.children) {
			solveNamespacePrefixes(c, map);
		}

		map.subList(size, map.size()).clear();
	}

	private static void solveNamespacePrefixes(XMLAttribute a, List<String[]> map) {
		if (a.namespace != null) {
			String prefix = findPrefix(map, a.namespace);
			if (prefix == null) {
//				System.out.println("WARNING: namespace \"" + a.namespace + "\" was never defined or was not accessable");
			} else {
				a.prefix = prefix.isEmpty() ? null : prefix;
			}
		}
	}

	private static String findPrefix(List<String[]> map, String namespace) {
//		System.out.println("looking for " + namespace);
		ListIterator<String[]> it = map.listIterator(map.size());
		String[] ns;
		while (it.hasPrevious() && ((ns = it.previous()) != null)) {
//			System.out.println("loop: " + ns[0] + " - " + ns[1]);
			if (Objects.equals(ns[1], namespace)) {
				if (!obstructed(map, it.nextIndex() + 1, ns[0])) {
					if (ns[0] == null)
						return "";
					return ns[0];
				} else {
//					System.out.println(ns[0] + " was obstructed");
				}
			}
		}
		return null;
	}

	private static boolean obstructed(List<String[]> map, int index, String prefix) {
		for (int i = index; i < map.size(); i++) {
//			System.out.println(" obstruction test: " + map.get(i)[0]);
			if (Objects.equals(map.get(i)[0], prefix)) {
				return true;
			}
		}
		return false;
	}

	public static void resetNamespaces(XMLElement root) {

		final Occurances<String> occurances = new Occurances<>();

		XMLHierarchyIterator.iterate(new Receiver() {
			@Override
			public void receive(XMLElement element) {
				occurances.add(element.getNamespaceURI());
				Iterator<XMLAttribute> it = element.getAttributes().iterator();
				XMLAttribute a;
				while (it.hasNext() && ((a = it.next()) != null)) {
					if (a.isNamespaceDeclaration()) {
						it.remove();
					} else {
						if (a.getNamespaceURI() != null)
							occurances.add(a.getNamespaceURI());
					}
				}
			}
		}, root);

		final Map<String, String> prefixMap = new HashMap<String, String>();

		boolean allowDefaultNS = !occurances.containsKey(null);
		occurances.remove(null);
		int i = 1;
		for (Entry<String, Integer> e : occurances.entrySet()) {
			String prefix;
			if ((prefix = defaultNSPrefix.get(e.getKey())) != null) {
				prefixMap.put(e.getKey(), prefix);
			} else {
				prefixMap.put(e.getKey(), "ns" + (i++));
			}
		}
		prefixMap.remove(null);

		// Allow use of default ns?
		if (allowDefaultNS) {
			String def = occurances.getMostCommon();
			prefixMap.put(def, null);
		}

		XMLHierarchyIterator.iterate(new Receiver() {
			@Override
			public void receive(XMLElement element) {
				element.setPrefix(prefixMap.get(element.getNamespaceURI()));
				for (XMLAttribute a : element.getAttributes())
					a.setPrefix(prefixMap.get(a.getNamespaceURI()));
			}
		}, root);

		for (Entry<String, String> e : prefixMap.entrySet()) {
			root.declareNamespace(e.getValue(), e.getKey());
		}
	}

	public static XMLElement createSOAPFrame() {
		XMLElement root = new XMLElement("Envelope", SOAP_ENVELOPE);
		root.declareNamespace("soap", SOAP_ENVELOPE);

		root.addAttribute("encodingStyle", SOAP_ENCODING, SOAP_ENVELOPE);

		root.addChild("Header", SOAP_ENVELOPE);
		root.addChild("Body", SOAP_ENVELOPE);

		return root;
	}
	
	public static String printOverview(final XMLElement element) {
		byte[] data = StreamCatcher.from(new StreamWriter() {
			@Override
			public void writeToStream(OutputStream stream) throws IOException {
				writeElementOverview(element, stream, 0);
			}
		}).toByteArray();
		return new String(data, element.tree.getCharset());
	}
	
	public static void writeElementOverview(XMLElement element, OutputStream stream, int level) throws IOException {
		byte[] tabs = XMLUtils.level(level);
		stream.write(tabs);
		stream.write('<');
		byte[] qname = element.getQualifiedName().getBytes(element.tree.getCharset());
		stream.write(qname);

		if (element.hasAttributes()) {
			Iterator<XMLAttribute> it = element.attributes.iterator();
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

		if (element.hasChildren()) {
			stream.write('>');
			stream.write('\n');

			for (XMLElement child : element.children) {
				writeElementOverview(child, stream, level + 1);
			}

			stream.write(tabs);
			stream.write('<');
			stream.write('/');
			stream.write(qname);
		} else if (element.hasValue()) {
			stream.write('>');
			
			byte[] data;
			
			if (element.cdata) {
				data = XMLUtils.convertToCDATA(element.value);
			} else {
				String d = XMLUtils.escape(element.getValue());
				d = d.replaceAll("\r", "\\r").replaceAll("\n", "\\n");
				data = d.getBytes(element.tree.getCharset());
			}
			
			if (data.length > 50) {
				stream.write(data, 0, Math.min(data.length, 50));
				stream.write(("[+" + (data.length - 50) + "b]").getBytes());
			}else {
				stream.write(data);
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

	/**
	 * public static XMLObject createSOAPFrame() { String soap =
	 * "http://www.w3.org/2003/05/soap-envelope/";
	 * 
	 * XMLObject root = new XMLObject("Envelope"); root.setNamespace(soap);
	 * 
	 * root.declareNamespace(soap, "soap"); root.addAttribute("encodingStyle",
	 * "http://www.w3.org/2003/05/soap-encoding").setNamespace(soap);
	 * 
	 * root.addChild("Header").setNamespace(soap);
	 * root.addChild("Body").setNamespace(soap);
	 * 
	 * return root; }
	 * 
	 */
}