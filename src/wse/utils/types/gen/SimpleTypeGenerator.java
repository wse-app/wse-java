package wse.utils.types.gen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import wse.utils.StringUtils;
import wse.utils.source.Source;
import wse.utils.xml.XMLElement;
import wse.utils.xml.XMLUtils;

public class SimpleTypeGenerator {

	private static Map<String, XMLElement> elementMap = new HashMap<>();
	private static Map<String, SimpleType> map = new HashMap<>();
	private static Map<String, String> typeDefLines = new HashMap<>();

	private static class SimpleType {
		private static String[] typeDefDef = { "baseType", "parse", "print" };
		private Map<String, String> typeDef = new HashMap<>();

		XMLElement restriction;

		private SimpleType(String type) {
			map.put(type, this);
			typeDef.put("type", type);

			typeDef.put("baseType", "String");
			typeDef.put("parse", "");

			String line = StringUtils.WS_collapse(typeDefLines.get(type));
			if (line != null) {
				String[] split = line.split(" ");
				for (int i = 0; i < split.length; i++) {
					typeDef.put(typeDefDef[i], split[i]);
				}
			}

			XMLElement element = elementMap.get(type);
			restriction = element.getChild("restriction");

			try {
				typeDef.put("restriction:base", qname(restriction.getAttribute("base").getValue()));				
			}catch(Exception e) {
//				System.out.println(element);
			}
		}

		public String type() {
			return typeDef.get("type");
		}

		public String baseType() {
			return typeDef.get("baseType");
		}

		public String parse() {
			return typeDef.get("parse");
		}

		public String print() {
			return typeDef.get("print");
		}

		public String restrictionBase() {
			return typeDef.get("restriction:base");
		}

		static List<String> rest_new = Arrays.asList("minInclusive", "maxInclusive", "minExclusive", "maxExclusive",
				"enumeration");
		static List<String> rest_quote = Arrays.asList("pattern", "whiteSpace");
		static List<String> rest_raw = Arrays.asList("length", "minLength", "maxLength", "totalDigits", "fractionDigits");
		
		public void write(PrintStream stream) {

			String restrictionBase = restrictionBase();
			String type = type();
			String baseType = baseType();
			String parse = parse();
			String print = print();
			if ("anySimpleType".equals(restrictionBase)) {
				stream.println("public static class xsd_" + type + " extends AnySimpleType<" + baseType + "> {");
			} else {
				stream.println(
						"public static class xsd_" + type + " extends AnySimpleType.xsd_" + restrictionBase + " {");
			}

			stream.println("\tpublic xsd_" + type + "(String value) { super(value); restrictions(); }");
			if (!"String".equals(baseType))
				stream.println(
						"\tpublic xsd_" + type + "(" + baseType + " value) { super(value, null); restrictions(); }");
			{
				stream.println("\tprivate void restrictions() {");
				stream.println("\t\trestriction();");
				for (XMLElement r : restriction.getChildren()) {
					String name = r.getName();
					if (rest_new.contains(name)) {
						stream.println("\t\t" + r.getName() + "(" + parse + "(\"" + r.getAttribute("value").getValue()
								+ "\"));");
					} else if (rest_quote.contains(name)) {
						stream.println("\t\t" + r.getName() + "(\"" + r.getAttribute("value").getValue() + "\");");
					} else if (rest_raw.contains(name)){
						stream.println("\t\t" + r.getName() + "(" + r.getAttribute("value").getValue() + ");");
					}
				}
				stream.println("\t}");
			}
			{
				if (parse != null)
					stream.println("\tpublic " + baseType + " parse(String value) { return " + parse + "(value); }");
				if (print != null)
					stream.println("\tpublic String print(" + baseType + " value) { return " + print + "(value); }");

			}

			stream.println("}");
		}
	}

	public static String qname(String QName) {
		return QName.substring(QName.indexOf(':') + 1);
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {

		String db = Source.getContainingText(SimpleTypeGenerator.class.getResourceAsStream("builtInTypes.txt"));
		for (String line : db.split("\r\n")) {
			line = StringUtils.WS_collapse(line);
			String type = line.split(" ")[0];
			typeDefLines.put(type, line.substring(line.indexOf(' ') + 1));
		}

		XMLElement schema = XMLUtils.parse(SimpleTypeGenerator.class.getResourceAsStream("builtInTypes.xml"));

		for (XMLElement e : schema.getChildren("simpleType")) {
			String type = e.getAttribute("name").getValue();
			elementMap.put(type, e);
			new SimpleType(type);
		}
		
		map.get("token").write(System.out);
		
	}

	public static void writeType() {

	}
}
