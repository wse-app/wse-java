package server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;
import wse.server.WSEServer;
import wse.utils.ClassUtils;
import wse.utils.config.Config;
import wse.utils.config.WseConfig;

public class PublicFolderServer extends WSEServer {

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

		try {
			if (WseConfig.saveDefault(iniFile))
				return;
		} catch (IOException e) {
			log.severe("Failed to save default ini file: " + e.getMessage());
			e.printStackTrace();
		}

		Config config;

		try {
			config = WseConfig.parse(iniFile, Charset.forName("UTF-8"));
		} catch (Exception e) {
			log.severe("Failed to parse '" + iniFile.getName() + "': " + e.getMessage());
			e.printStackTrace();
			return;
		}

		try {
			WseConfig.init(config);
		} catch (Exception e) {
			log.severe("Failed global setup: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		try {
			new PublicFolderServer(config);
		} catch (Exception e) {
			log.severe("Failed to start server");
			e.printStackTrace();
			return;
		}
	}

	public PublicFolderServer(Config ini) {
		boolean useful = WseConfig.load(this, root, ini);
		
		if (!useful) {
			log.severe("PublicFolderServer does not have any servlets registered and will terminate.");
			return;
		}
		
		start();
	}

}
