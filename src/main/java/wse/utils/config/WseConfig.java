package wse.utils.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import wse.utils.FileUtils;
import wse.utils.exception.WseParsingException;
import wse.utils.internal.InternalFormat;
import wse.utils.ssl.SSLAuth;

public class WseConfig extends Config {

	private static final Logger log = WSE.getLogger();

	public static final String NS_WSE = "WSE";
	public static final String NS_WSE_HTTP = "WSE.Http";
	public static final String NS_WSE_HTTPS = "WSE.Https";
	public static final String NS_WSE_SHTTP = "WSE.SHttp";
	public static final String NS_WSE_PUBLICFOLDER = "WSE.PublicFolder";

	/**
	 * 
	 * Saves the default wse ini file to the target location, if it does not already
	 * exist.
	 * 
	 * @param target The file to save a default file at
	 * @return true if the target file does not exist and a default file was saved
	 *         there successfully.
	 * @throws IOException
	 */
	public static boolean saveDefault(File target) throws IOException {
		if (target.exists())
			return false;

		log.info("Generating default configuration file.");

		boolean result;

		try (InputStream input = WseConfig.class.getResourceAsStream("/default.ini")) {
			result = saveDefault(target, input);
		}

		log.info("Edit '" + target.getName() + "' and start the server again.");

		return result;
	}

	/**
	 * Saves the source file to the target location, if it does not already exist.
	 * 
	 * @param target The file to save a default file at
	 * @param source The default file to save.
	 * @return true if the target file does not exist and a default file was saved
	 *         there successfully.
	 * @throws IOException
	 */
	public static boolean saveDefault(File target, InputStream source) throws IOException {
		if (target.exists())
			return false;

		return Config.saveDefault(target, source);
	}

	public static WseConfig parse(File file, Charset cs) throws FileNotFoundException, IOException {
		return new WseConfig(file, cs);
	}

	public WseConfig(File target, Charset cs) throws FileNotFoundException, IOException {
		super(InternalFormat.parse(target, cs));
	}

	public void load(WSEServer server, File rootDirectory) {
		load(server, rootDirectory, this);
	}

	public static void init(Config config) {
		String appName = config.getValue("AppName", NS_WSE);
		if (appName != null) {
			WSE.declareApplicationName(appName);
		}

		String logLevel = config.getValue("LogLevel", NS_WSE);
		if (logLevel != null) {
			try {
				Level lvl = Level.parse(logLevel);
				WSE.setLogLevel(lvl);
			} catch (Exception e) {
				throw new WseParsingException("Invalid log Level: " + logLevel, e);
			}
		}
	}

	public static boolean load(WSEServer server, File rootDirectory, Config config) {
		boolean useful = true;

		loadPorts(server, rootDirectory, config);
		useful &= loadPublicFolder(server, rootDirectory, config);

		return useful;
	}

	private static void loadPorts(WSEServer server, File rootDirectory, Config config) {
		Integer httpPort = config.getInt("Port", NS_WSE_HTTP, null);
		Integer httpsPort = config.getInt("Port", NS_WSE_HTTPS, null);
		Integer shttpPort = config.getInt("Port", NS_WSE_SHTTP, null);
		Integer shttpsPort = config.getInt("HttpsPort", NS_WSE_SHTTP, httpsPort);

		String httpsKeyStorePath = config.getValue("KeyStorePath", NS_WSE_HTTPS);
		String httpsKeyStorePass = config.getValue("KeyStorePassphrase", NS_WSE_HTTPS);
		String shttpKeyStorePath = config.getValue("KeyStorePath", NS_WSE_SHTTP);
		String shttpKeyStorePass = config.getValue("KeyStorePassphrase", NS_WSE_SHTTP);

		if (shttpKeyStorePath == null) {
			shttpKeyStorePath = httpsKeyStorePath;
			shttpKeyStorePass = httpsKeyStorePass;
		}

		if (httpPort != null) {
			server.addHttp(httpPort);
		}

		SSLAuth httpsAuth = null;
		if (httpsKeyStorePath != null) {
			char[] pass = httpsKeyStorePass != null ? httpsKeyStorePass.toCharArray() : new char[0];
			httpsAuth = SSLAuth.fromKeyStore(FileUtils.getAbsolute(rootDirectory, httpsKeyStorePath), pass);
		}

		if (httpsPort != null) {
			server.addHttps(httpsPort, httpsAuth);
		}

		SSLAuth shttpAuth = httpsAuth;
		if (shttpKeyStorePath != null) {
			char[] pass = shttpKeyStorePass != null ? shttpKeyStorePass.toCharArray() : new char[0];
			shttpAuth = SSLAuth.fromKeyStore(FileUtils.getAbsolute(rootDirectory, shttpKeyStorePath), pass);
		}

		if (shttpPort != null) {
			server.addSHttp(shttpsPort, shttpAuth, shttpPort);
		}
	}

	private static boolean loadPublicFolder(WSEServer server, File rootDirectory, Config config) {
		Collection<String> docRoots = config.getValueArray("PublicFolderRoot", NS_WSE_PUBLICFOLDER);
		String defaultPath = config.getValue("DefaultPath", NS_WSE_PUBLICFOLDER);
		boolean allowRead = config.getBool("AllowRead", NS_WSE_PUBLICFOLDER, true);
		boolean allowWrite = config.getBool("AllowWrite", NS_WSE_PUBLICFOLDER, false);
		boolean allowCors = config.getBool("AllowCORS", NS_WSE_PUBLICFOLDER, true);

		if (docRoots == null || docRoots.size() == 0) {
			return false;
		}

		ServiceManager manager = server.getServiceManager();

		Collection<File> files = new LinkedList<>();
		for (String docRoot : docRoots) {
			File file = FileUtils.getAbsolute(rootDirectory, docRoot);
			files.add(file);
		}

		PublicFolderServlet servlet = new PublicFolderServlet(files);
		servlet.setCORSAllowAll(allowCors);

		servlet.setDefaultPath(defaultPath);

		servlet.setAllowRead(allowRead);
		servlet.setAllowWrite(allowWrite);

		manager.registerDefault(servlet);
		return true;
	}
}
