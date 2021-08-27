package wse.server.shttp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import wse.utils.exception.SHttpException;
import wse.utils.shttp.SKey;

public class SHttpServerSessionStore {

	private static Random random = new Random();

	public static int SHTTP_SESSION_LIFE_LENGTH = 3600;

	private Map<String, SKey> sessions = Collections.synchronizedMap(new HashMap<String, SKey>(100));

	public SKey getKey(String name) {
		return sessions.get(name);
	}

	public void storeKey(SKey key) {
		sessions.put(key.getKeyName(), key);
	}

	public static SKey generateKey(int bitLength) {
		String name = UUID.randomUUID().toString();
		byte[] key = genByteArray(bitLength);

		return new SKey(name, key, SHTTP_SESSION_LIFE_LENGTH);
	}

	public static byte[] genByteArray(int bitLength) {
		if (bitLength % 8 != 0)
			throw new SHttpException("Illegal key size: " + bitLength);

		random.setSeed(random.nextLong());

		byte[] result = new byte[bitLength / 8];
		random.nextBytes(result);
		return result;
	}
}
