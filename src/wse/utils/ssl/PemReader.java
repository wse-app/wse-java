package wse.utils.ssl;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import wse.WSE;

@Deprecated
public final class PemReader {
	private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" + // Header
			"([a-z0-9+/=\\r\\n]+)" + // Base64 text
			"-+END\\s+.*CERTIFICATE[^-]*-+", // Footer
			CASE_INSENSITIVE);

	@SuppressWarnings("unused")
	private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" + // Header
			"([a-z0-9+/=\\r\\n]+)" + // Base64 text
			"-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", // Footer
			CASE_INSENSITIVE);

	static {
//		Security.addProvider(new BouncyCastleProvider());
	}

	private PemReader() {
	}

	public static KeyStore loadTrustStore(InputStream certificateChainFile)
			throws IOException, GeneralSecurityException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);

		List<X509Certificate> certificateChain = readCertificateChain(certificateChainFile);
		for (X509Certificate certificate : certificateChain) {
			X500Principal principal = certificate.getSubjectX500Principal();
			keyStore.setCertificateEntry(principal.getName("RFC2253"), certificate);
		}
		return keyStore;
	}

	@Deprecated
	public static KeyStore loadKeyStore(InputStream certificateChainFile, InputStream privateKeyFile, char[] keyPhrase)
			throws IOException, GeneralSecurityException {

		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);

		loadKeyStore(keyStore, "key", certificateChainFile, privateKeyFile, keyPhrase);

		return keyStore;
	}

	@Deprecated
	public static void loadKeyStore(KeyStore keyStore, String alias, InputStream certificateChainFile,
			InputStream privateKeyFile, char[] keyPhrase) throws IOException, GeneralSecurityException {

		throw new UnsupportedOperationException("Not supported, please load in complete keystores instead");

//		PrivateKey key;
//		try {
//			key = readKey(privateKeyFile, keyPhrase);
//		} catch (OperatorCreationException | PKCSException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		List<X509Certificate> certificateChain = readCertificateChain(certificateChainFile);
//		if (certificateChain.isEmpty()) {
//			throw new CertificateException(
//					"Certificate file does not contain any certificates: " + certificateChainFile);
//		}
//
//		keyStore.setKeyEntry(alias, key, (keyPhrase == null ? "".toCharArray() : keyPhrase),
//				certificateChain.toArray(new X509Certificate[0]));
	}

	private static List<X509Certificate> readCertificateChain(InputStream certificateChainFile)
			throws IOException, GeneralSecurityException {
		String contents = readFile(certificateChainFile);

		Matcher matcher = CERT_PATTERN.matcher(contents);
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		List<X509Certificate> certificates = new ArrayList<>();

		int start = 0;
		while (matcher.find(start)) {
			byte[] buffer = base64Decode(matcher.group(1));
			certificates
					.add((X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(buffer)));
			start = matcher.end();
		}

		return certificates;
	}

//	private static PKCS8EncodedKeySpec readPrivateKey(InputStream keyFile, char[] keyPhrase)
//			throws IOException, GeneralSecurityException {
//		String content = readFile(keyFile);
//
//		Matcher matcher = KEY_PATTERN.matcher(content);
//		if (!matcher.find()) {
//			throw new KeyStoreException("found no private key: " + keyFile);
//		}
//		byte[] encodedKey = base64Decode(matcher.group(1));
//
//		if (keyPhrase == null) {
//			return new PKCS8EncodedKeySpec(encodedKey);
//		}
//
//		EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(encodedKey);
//	
//		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
//		SecretKey secretKey = keyFactory.generateSecret(new PBEKeySpec(keyPhrase));
//
//		Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
//		cipher.init(DECRYPT_MODE, secretKey, encryptedPrivateKeyInfo.getAlgParameters());
//
//		return encryptedPrivateKeyInfo.getKeySpec(cipher);
//	}

	@SuppressWarnings("unused")
	@Deprecated
	private static PrivateKey readKey(InputStream stream, char[] keyPhrase)
			throws KeyStoreException, IOException/* , OperatorCreationException, PKCSException */ {

		throw new UnsupportedOperationException("Not supported, please load in complete keystores instead");

//		String content = readFile(stream);
//
//		Matcher matcher = KEY_PATTERN.matcher(content);
//		if (!matcher.find()) {
//			throw new KeyStoreException("found no private key.");
//		}
//		
////		byte[] encodedKey = base64Decode();
//		
//		PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(matcher.group().getBytes())));
//
//		PKCS8EncryptedPrivateKeyInfo encPKInfo = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
//
//		InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC")
//				.build(keyPhrase);
//		PrivateKeyInfo pkInfo = encPKInfo.decryptPrivateKeyInfo(decProv);
//
//		PrivateKey privKey = new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pkInfo);
//		
//		pemParser.close();
//		return privKey;

	}

	private static byte[] base64Decode(String base64) {
		return WSE.parseBase64Binary(base64);
	}

	private static String readFile(InputStream stream) throws IOException {
		try (Reader reader = new InputStreamReader(stream, US_ASCII)) {
			StringBuilder stringBuilder = new StringBuilder();

			CharBuffer buffer = CharBuffer.allocate(2048);
			while (reader.read(buffer) != -1) {
				buffer.flip();
				stringBuilder.append(buffer);
				buffer.clear();
			}
			return stringBuilder.toString();
		}
	}
}