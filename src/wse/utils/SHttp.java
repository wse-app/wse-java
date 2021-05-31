package wse.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import wse.WSE;
import wse.utils.exception.WseSHttpDecryptionException;
import wse.utils.exception.WseSHttpEncryptionException;
import wse.utils.exception.WseSHttpException;
import wse.utils.http.HttpHeader;
import wse.utils.http.HttpMethod;
import wse.utils.http.HttpRequestLine;
import wse.utils.http.HttpStatusLine;
import wse.utils.http.HttpURI;
import wse.utils.http.StreamUtils;
import wse.utils.log.Loggers;
import wse.utils.shttp.SKey;
import wse.utils.stream.RIMInputStream;
import wse.utils.stream.WseInputStream;

public final class SHttp {
	
	public static boolean LOG_ENCRYPTED_DATA = false;
	public static final String SECURE_HTTP14 = "Secure-HTTP/1.4";
	public static final String INIT_PATH = "/InitShttpSession";
	public static int[] SUPPORTED_KEY_LENGTHS = new int[] { 128 };
	public static final String KEY_NAME_ATTRIBUTE = "Prearranged-Key-Info";
	private static Random random = new Random();

	private static final Logger log = WSE.getLogger();
	
	public static Cipher getCipher(SKey key, int opmode) {
		try {
			SecretKeySpec newKey = new SecretKeySpec(key.getKey(), 0, key.getKey().length, "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding"); /// CBC/NoPadding
			cipher.init(opmode, newKey);

			return cipher;

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new WseSHttpEncryptionException("Encryption failed: " + e.getMessage(), e);
		} catch (InvalidKeyException e) {
			throw new WseSHttpEncryptionException("Failed to encrypt: Invalid encryption key: " + e.getMessage(), e);
		}
	}

	public static byte[] genByteArray(int bitLength) {
		if (bitLength % 8 != 0)
			throw new WseSHttpException("Illegal key size: " + bitLength);

		random.setSeed(System.nanoTime());

		byte[] result = new byte[bitLength / 8];
		random.nextBytes(result);
		return result;
	}

	public static boolean keyLengthSupported(int bitLength) {
		if (SUPPORTED_KEY_LENGTHS == null)
			return false;

		for (int i : SUPPORTED_KEY_LENGTHS)
			if (i == bitLength)
				return true;
		return false;
	}

	public static String getKeyLengthsSupported() {
		if (SUPPORTED_KEY_LENGTHS.length == 0)
			return "";

		StringBuilder result = new StringBuilder(40);
		result.append(SUPPORTED_KEY_LENGTHS[0] + "");

		for (int i = 1; i < SUPPORTED_KEY_LENGTHS.length; i++) {
			result.append(", ").append(SUPPORTED_KEY_LENGTHS[i] + "");
		}
		return result.toString();
	}

	public static HttpHeader makeSHttpHeader(String keyName) {
		HttpHeader head = new HttpHeader(makeRequestLine());

		head.setAttribute("Connection", "close");
		head.setAttribute("Prearranged-Key-Info", "outband:" + keyName);
		head.setContentType(MimeType.message.http);
		return head;
	}

	public static byte[] decrypt(byte[] data, SKey key) {
		return decrypt(data, 0, data.length, key);
	}
	public static byte[] decrypt(byte[] data, int offset, int length, SKey key) {
		Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
		try {
			return cipher.doFinal(data, offset, length);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new WseSHttpDecryptionException("Failed to decrypt: " + e.getMessage(), e);
		}
	}
	
	public static byte[] encrypt(byte[] data, SKey key) {
		return encrypt(data, 0, data.length, key);
	}
	public static byte[] encrypt(byte[] data, int offset, int length, SKey key) {
		Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);
		try {
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new WseSHttpEncryptionException("Failed to encrypt: " + e.getMessage(), e);
		}
	}
	
	public static WseInputStream sHttpDecryptData(InputStream stream, SKey key) throws IOException {
		return sHttpDecryptData(StreamUtils.readAll(stream), key);
	}
	
	public static WseInputStream sHttpDecryptData(byte[] data, SKey key) throws IOException {
		log.finest("Decrypting " + data.length + " bytes");
		if (SHttp.LOG_ENCRYPTED_DATA) {
			Loggers.hexdump(log, Level.FINEST, data);
		}
		
		if (data.length % key.getBlockSize() != 0) {
			int len = data.length - data.length % key.getBlockSize();
			if (len > 0) {
				byte[] decrypted_part = decrypt(data, 0, data.length - data.length % key.getBlockSize(), key);
				log.finest("Decrypted part: " + len + " bytes");
				Loggers.hexdump(log, Level.FINEST, decrypted_part);
				throw new WseSHttpDecryptionException("Data not multiple of block length: " + data.length);
			}
		}
		
		byte[] decrypted = decrypt(data, key);
		return new RIMInputStream(new ByteArrayInputStream(decrypted), key.getBlockSize(), key.getInjectionSize());
	}

	public static HttpRequestLine makeRequestLine() {
		return new HttpRequestLine(HttpMethod.SECURE, HttpURI.raw("*"), SHttp.SECURE_HTTP14);
	}
	
	public static HttpStatusLine makeStatusLine(int code) {
		HttpStatusLine r = new HttpStatusLine(code);
		r.setHttpVersion(SECURE_HTTP14);
		return r;
	}

	public static HttpRequestLine makeInitRequestLine(int keyLength) {
		return new HttpRequestLine(HttpMethod.GET, HttpURI.fromURI(INIT_PATH + "?method=AES" + keyLength),
				HttpRequestLine.HTTP11);
	}
}