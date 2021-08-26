package wse.utils.ini;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import wse.utils.internal.IParser;
import wse.utils.options.IOptions;

public class IniUtils {

	public static final IParser<IniFile> INI_PARSER = new IParser<IniFile>() {
		@Override
		public IniFile parse(InputStream input, Charset cs, IOptions options) throws IOException {
			return IniTokenizer.parse(input, cs, options);
		}

		@Override
		public IniFile createEmpty() {
			return new IniFile();
		}
	};

}
