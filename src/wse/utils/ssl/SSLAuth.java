package wse.utils.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import wse.utils.exception.WseException;

public class SSLAuth {

	private KeyStore keystore;
	private SSLContext context;
	private WSETrustManager trustmanager;

	private SSLAuth() {

	}

	public KeyStore getKeyStore() {
		return keystore;
	}

	public SSLContext getContext() {
		return context;
	}

	public SSLSocketFactory getSSLSocketFactory() {
		return context.getSocketFactory();
	}

	public SSLServerSocketFactory getSSLServerSocketFactory() {
		return context.getServerSocketFactory();
	}

	public WSETrustManager getTrustManagerImpl() {
		return trustmanager;
	}

	public static SSLAuth fromKeyStore(File input, char[] passphrase) {
		try {
			return fromKeyStore(KeyStore.getDefaultType(), new FileInputStream(input), passphrase);
		} catch (FileNotFoundException e) {
			throw new WseException(e.getMessage(), e);
		}
	}

	public static SSLAuth fromKeyStore(InputStream input, char[] passphrase) {
		return fromKeyStore(KeyStore.getDefaultType(), input, passphrase);
	}

	public static SSLAuth fromKeyStore(String keyStoreType, InputStream input, char[] passPhrase) {
		KeyStore ks = null;
		try {

			ks = KeyStore.getInstance(keyStoreType);

			ks.load(input, passPhrase);

			return make(ks, passPhrase);

		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| KeyManagementException | UnrecoverableKeyException e) {

			try {
				input.close();
			} catch (IOException ex) {
			}

			throw new WseException(e.getMessage(), e);

		}
	}

	public static SSLAuthBuilder fromCertificate(String alias, InputStream input)
			throws GeneralSecurityException, IOException {

		SSLAuthBuilder builder = new SSLAuthBuilder(KeyStore.getDefaultType());
		builder.addCertificate(alias, input);
		return builder;

	}

	public static SSLAuthBuilder builder() {
		try {
			return new SSLAuthBuilder(KeyStore.getDefaultType());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new WseException(e.getMessage(), e);
		}
	}

	public static SSLAuth fromKeyStore(KeyStore keystore, char[] password) {
		try {
			return fromKeyStore(keystore).make(password);
		} catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException e) {
			throw new WseException(e.getMessage(), e);
		}
	}

	public static SSLAuthBuilder fromKeyStore(KeyStore keystore) {
		return new SSLAuthBuilder(keystore);
	}

	public static class SSLAuthBuilder {

		private KeyStore keystore;

		public SSLAuthBuilder(String keystoreType)
				throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
			this.keystore = KeyStore.getInstance(keystoreType);
			keystore.load(null, null);
		}

		public SSLAuthBuilder(KeyStore keystore) {
			this.keystore = keystore;
		}

		public SSLAuthBuilder addCertificate(String alias, InputStream stream) throws GeneralSecurityException {

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(stream);

			keystore.setCertificateEntry(alias, cert);

			return this;
		}

		/**
		 * Deprecated, load complete keystores instead
		 * 
		 */
		@Deprecated
		public SSLAuthBuilder addCertificate(String alias, InputStream certificate, InputStream key,
				char[] passPhrase) {
			try {
				PemReader.loadKeyStore(this.keystore, alias, certificate, key, passPhrase);
			} catch (IOException | GeneralSecurityException e) {
				e.printStackTrace();
			}
			return this;
		}

		public SSLAuth make(char[] passPhrase)
				throws KeyManagementException, KeyStoreException, UnrecoverableKeyException {

			return SSLAuth.make(this.keystore, passPhrase);

		}

	}

	public static final class CertificateChain {

		private ArrayList<Certificate> certs = new ArrayList<>();

		private CertificateChain() {
		}

		public static CertificateChain fromRoot(InputStream stream) {
			CertificateChain chain = new CertificateChain();
			return chain.next(stream);
		}

		public CertificateChain next(InputStream stream) {
			try {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				Certificate cert = cf.generateCertificate(stream);
				certs.add(cert);
			} catch (CertificateException e) {
				e.printStackTrace();
			}
			return this;
		}

		public Certificate[] toArray() {
			Certificate[] array = new Certificate[certs.size()];
			return this.certs.toArray(array);
		}
	}

	private static SSLAuth make(KeyStore ks, char[] keyPassPhrase)
			throws KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			SSLContext context;

			context = SSLContext.getInstance("TLS");

			X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
			WSETrustManager tm = new WSETrustManager(defaultTrustManager);

			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(ks, keyPassPhrase);

			context.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { tm }, new SecureRandom());

			SSLAuth store = new SSLAuth();
			store.keystore = ks;
			store.context = context;
			store.trustmanager = tm;

			return store;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

}
