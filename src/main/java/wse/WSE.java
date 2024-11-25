package wse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.utils.ClassUtils;
import wse.utils.StringUtils;
import wse.utils.exception.WseException;
import wse.utils.log.AndroidLoggingHandler;
import wse.utils.log.LogPrintStream;
import wse.utils.log.WseConsoleHandler;
import wse.utils.log.WseFileHandler;
import wse.utils.source.Source;

public final class WSE extends WSEUtils {

	private WSE() {
	}

	public static final String NS = "https://wse.app/ns/wse";
	public static final String LOG_FAMILY = "wse";
	public static final String VERSION;

	public static final boolean RUNNING_ANDROID;

	static {
		String val = System.getProperty("java.runtime.name");
		if ("android runtime".equalsIgnoreCase(val)) {
			RUNNING_ANDROID = true;
		} else {
			RUNNING_ANDROID = false;
		}

		String v = "?";
		try {
			v = Source.getContainingText(WSE.class.getResourceAsStream("/version"));
		} catch (Throwable t) {
			// ignore
		}
		VERSION = v;
	}

	private static String applicationName = "WebServiceEngine Application/" + VERSION;

	public static final PrintStream out = new LogPrintStream(getLogger(), Level.INFO);
	public static final PrintStream err = new LogPrintStream(getLogger(), Level.SEVERE);

	public static String getVersion() {
		return VERSION;
	};

	public static Logger getLogger() {
		return Logger.getLogger(LOG_FAMILY);
	}

	public static Logger getLogger(Class<?> clazz) {
		if (clazz == null)
			return getLogger();
		return Logger.getLogger(clazz.getName());
	}

	public static Logger getLogger(String child) {
		if (child == null)
			return getLogger();
		return Logger.getLogger(LOG_FAMILY + "." + child);
	}

	public static void initDefaultStandaloneLogging() {

		Logger log = getLogger();
		log.setUseParentHandlers(false);

		if (RUNNING_ANDROID) {
			try {
				AndroidLoggingHandler.addToLogger(log);
				return;
			} catch (Throwable t) {
				// ignore, TODO return?
			}
		}

		WseConsoleHandler.addToLogger(log);
	}

	public static void initFileLogging() throws SecurityException, FileNotFoundException {
		initFileLogging(WSE.class);
	}

	public static void initFileLogging(int daysKeep) throws SecurityException, FileNotFoundException {
		initFileLogging(WSE.class, daysKeep);
	}

	public static void initFileLogging(Class<?> clazz) {
		initFileLogging(new File(ClassUtils.getJarFile(clazz).getParentFile(), "logs"));
	}

	public static void initFileLogging(Class<?> clazz, int daysKeep) {
		initFileLogging(new File(ClassUtils.getJarFile(clazz).getParentFile(), "logs"), daysKeep);
	}

	public static void initFileLogging(File parentDirectory) {
		initFileLogging(parentDirectory, -1);
	}

	public static void initFileLogging(File parentDirectory, int daysKeep) {
		Logger log = getLogger();
		try {
			log.addHandler(new WseFileHandler(parentDirectory, daysKeep));
		} catch (SecurityException | FileNotFoundException e) {
			throw new WseException("Failed to enable file logging: " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * Set the log level of the WSE client and server
	 * 
	 * @param lvl
	 */
	public static void setLogLevel(Level lvl) {
		getLogger().setLevel(lvl);
	}

	public static void declareApplicationName(String name) {
		WSE.applicationName = name;
	}

	public static String getApplicationName() {
		return applicationName;
	}

	public static boolean parseBool(String s) {
		return ("1".equals(s) || "true".equalsIgnoreCase(s));
	}

	public static Double parseDouble(String d) {
		if (d == null)
			return null;
		if (d.isEmpty())
			return null;
		if (d.equalsIgnoreCase("nan")) {
			return Double.NaN;
		}
		return Double.parseDouble(d);
	}

	public static byte[] parseBase64Binary(String base64) {
		return StringUtils.parseBase64Binary(base64);
	}

	public static byte[] parseHexBinary(String hex) {
		return StringUtils.parseHexBinary(hex);
	}

	public static String printBase64Binary(byte[] data) {
		return StringUtils.printBase64Binary(data);
	}

	public static String printHexBinary(byte[] data) {
		return StringUtils.printHexBinary(data);
	}

	public static <T> String allStrings(T value) {
		if (value == null)
			return null;
		return String.valueOf(value);
	}

	public static String urlEncode(String src) {
		if (src == null)
			throw new NullPointerException();
		try {
			return URLEncoder.encode(src, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return src;
		}
	}

	public static String urlDecode(String src) {
		if (src == null)
			throw new NullPointerException();
		try {
			return URLDecoder.decode(src, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return src;
		}
	}

}
