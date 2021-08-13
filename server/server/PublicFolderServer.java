package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.ServiceManager;
import wse.server.WSEServer;
import wse.server.servlet.PublicFolderServlet;
import wse.utils.ClassUtils;
import wse.utils.http.StreamUtils;
import wse.utils.ini.IniException;
import wse.utils.ini.IniFile;
import wse.utils.ini.IniSection;
import wse.utils.ini.IniTokenizer;
import wse.utils.ssl.SSLAuth;

public class PublicFolderServer extends WSEServer {

	private static final String NS_WSE = "WSE";
	private static final String NS_WSE_HTTP = "WSE.Http";
	private static final String NS_WSE_HTTPS = "WSE.Https";
	private static final String NS_WSE_SHTTP = "WSE.SHttp";
	private static final String NS_WSE_PUBLICFOLDER = "WSE.PublicFolder";

	private static final Logger log = WSE.getLogger();

	private static File root;

	public static void main(String[] args) {

		WSE.initDefaultStandaloneLogging();
		WSE.setLogLevel(Level.FINEST);

		File jarFile = ClassUtils.getJarFile(PublicFolderServer.class);

		if (jarFile == null) {
			log.severe("Failed to determine jar file location");
			return;
		}

		root = jarFile.getParentFile();
		if (root == null) {
			log.severe("Failed to determine jar folder location");
			return;
		}

		File iniFile = new File(root, "wse.ini");
		if (!iniFile.exists()) {
			try {
				saveDefaultIni(iniFile);
			} catch (Exception e) {
				log.severe("Failed to save default ini file: " + e.getMessage());
				e.printStackTrace();
			}
			return;
		}

		IniFile ini;
		try (InputStream iniStream = new FileInputStream(iniFile)) {
			ini = IniTokenizer.parse(iniStream, Charset.forName("UTF-8"));
		} catch (IniException e) {
			log.severe("Failed to parse wse.ini: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (Exception e) {
			log.severe("Failed to read wse.ini: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		try {
			globalSetup(ini);
		} catch (Exception e) {
			log.severe("Failed global setup: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		try {			
			new PublicFolderServer(ini);
		} catch (Exception e) {
			log.severe("Failed to start server");
			e.printStackTrace();
			return;
		}
	}

	private static void saveDefaultIni(File file) throws Exception {
		file.createNewFile();
		try (FileOutputStream out = new FileOutputStream(file)) {
			try (InputStream in = PublicFolderServer.class.getResourceAsStream("default.ini")) {
				StreamUtils.write(in, out, 1024);
			}
		}
	}

	private static void globalSetup(IniFile ini) {

		IniSection wse = ini.getSection(NS_WSE);
		if (wse == null)
			return;

		String appName = wse.getValue("AppName");
		if (appName != null) {
			WSE.declareApplicationName(appName);
		}

		String logLevel = wse.getValue("LogLevel");
		if (logLevel != null) {
			try {
				Level lvl = Level.parse(logLevel);
				WSE.setLogLevel(lvl);
			} catch (Exception e) {
				log.severe("Invalid LogLevel: " + logLevel);
			}
		}

	}

	public PublicFolderServer(IniFile ini) {

		if (!initPorts(ini))
			return;

		if (!initPublicFolder(ini))
			return;

		start();
	}

	private boolean initPorts(IniFile ini) {
		Integer httpPort = parseInt(ini.getValue("Port", NS_WSE_HTTP), null);
		Integer httpsPort = parseInt(ini.getValue("Port", NS_WSE_HTTPS), null);
		Integer shttpPort = parseInt(ini.getValue("Port", NS_WSE_SHTTP), null);
		Integer shttpsPort = parseInt(ini.getValue("HttpsPort", NS_WSE_SHTTP), httpsPort);
		
		String httpsKeyStorePath = ini.getValue("KeyStorePath", NS_WSE_HTTPS);
		String httpsKeyStorePass = ini.getValue("KeyStorePassphrase", NS_WSE_HTTPS);
		String shttpKeyStorePath = ini.getValue("KeyStorePath", NS_WSE_SHTTP);
		String shttpKeyStorePass = ini.getValue("KeyStorePassphrase", NS_WSE_SHTTP);
		
//		if (shttpKeyStorePath == null) {
//			shttpKeyStorePath = httpsKeyStorePath;
//			httpsKeyStorePass = shttpKeyStorePass;
//		}
		
		if (httpPort != null) {
			addHttp(httpPort);
		}
		
		SSLAuth httpsAuth = null;
		if (httpsKeyStorePath != null) {
			char[] pass = httpsKeyStorePass != null ? httpsKeyStorePass.toCharArray() : new char[0];
			httpsAuth = SSLAuth.fromKeyStore(getAbsolute(root, httpsKeyStorePath), pass);
		}
		
		if (httpsPort != null) {
			addHttps(httpsPort, httpsAuth);
		}
		
		SSLAuth shttpAuth = httpsAuth;
		if (shttpKeyStorePath != null) {
			char[] pass = shttpKeyStorePass != null ? shttpKeyStorePass.toCharArray() : new char[0];
			shttpAuth = SSLAuth.fromKeyStore(getAbsolute(root, shttpKeyStorePath), pass);
		}
		
		if (shttpPort != null) {
			addSHttp(shttpsPort, shttpAuth, shttpPort);
		}
		

		return true;
	}

	private boolean initPublicFolder(IniFile ini) {
		Collection<String> docRoots = ini.getValueArray("PublicFolderRoot", NS_WSE_PUBLICFOLDER);
		String defaultPath = ini.getValue("DefaultPath", NS_WSE_PUBLICFOLDER);
		boolean allowRead = parseBool(ini.getValue("AllowRead", NS_WSE_PUBLICFOLDER), true);
		boolean allowWrite = parseBool(ini.getValue("AllowWrite", NS_WSE_PUBLICFOLDER), false);

		if (docRoots == null || docRoots.size() == 0) {
			log.severe("No public folder root specified");
			return false;
		}

		ServiceManager manager = getServiceManager();

		Collection<File> files = new LinkedList<>();
		for (String docRoot : docRoots) {
			File file = getAbsolute(root, docRoot);
			files.add(file);
		}

		PublicFolderServlet servlet = new PublicFolderServlet(files.toArray(new File[0]));

		servlet.setDefaultPath(defaultPath);
		servlet.setAllowRead(allowRead);
		servlet.setAllowWrite(allowWrite);

		manager.registerDefault(servlet);
		return true;
	}

	private static File getAbsolute(File root, String relative) {
		File tmp = new File(relative);
		if (tmp.isAbsolute())
			return tmp;
		
		return new File(root, relative);
	}

	private static Boolean parseBool(String value, Boolean def) {
		if (value == null)
			return def;
		return "1".equals(value) || "true".equalsIgnoreCase(value);
	}
	
	private static Integer parseInt(String value, Integer def) {
		if (value == null)
			return def;
		try {
			return Integer.parseInt(value);			
		} catch (Exception e) {
			log.severe("Invalid port: " + value + ", defaulting to " + def);
			return def;
		}
	}

}
