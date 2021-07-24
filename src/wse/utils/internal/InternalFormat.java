package wse.utils.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import wse.utils.MimeType;
import wse.utils.exception.WseBuildingException;
import wse.utils.exception.WseParsingException;
import wse.utils.json.JUtils;
import wse.utils.xml.XMLUtils;

public class InternalFormat {
	private static final Map<MimeType, IParser<?>> registered = new HashMap<>();

	static {
		InternalFormat.registerParser(JUtils.JSON_PARSER, MimeType.application.json);
		InternalFormat.registerParser(XMLUtils.XML_PARSER, MimeType.application.xml, MimeType.text.xml);
	}

	public static void registerParser(IParser<?> parser, MimeType... types) {
		for (MimeType mt : types) {
			registered.put(mt, parser);
		}
	}

	public static IElement parse(MimeType mt, InputStream input, Charset cs) throws IOException {
		IParser<? extends IElement> parser = registered.get(mt);

		if (parser == null)
			throw new WseParsingException(
					String.format("No suitable parser found for mimetype '%s'", mt.getFullName()));

		return parser.parse(input, cs);
	}

	public static IElement createEmpty(MimeType mt) {
		IParser<? extends IElement> parser = registered.get(mt);

		if (parser == null)
			throw new WseBuildingException(
					String.format("No suitable builder found for mimetype '%s'", mt.getFullName()));

		return parser.createEmpty();
	}

}
