package wse.server.shttp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import wse.WSE;
import wse.utils.SHttp;
import wse.utils.shttp.SKey;

public class SHttpSessionStore {

	public static int SHTTP_SESSION_LIFE_LENGTH = 3600;

	private Map<String, SKey> sessions = Collections.synchronizedMap(new HashMap<String, SKey>(100));

	private static byte[] fixedKey = null;

	public SKey getKey(String name) {
		SKey key = sessions.get(name);
		if (key != null)
			return key;
		return null;
	}

	public boolean hasExpired(String name) {
		return hasExpired(sessions.get(name));
	}

	public void store(SKey key) {
		sessions.put(key.getKeyName(), key);
	}

	public static SKey generateKey(int bitLength) {

		String name = UUID.randomUUID().toString();

		byte[] key = null;
		if (fixedKey != null) {
			key = new byte[bitLength / 8];
			if (fixedKey.length >= key.length) {
				System.arraycopy(fixedKey, 0, key, 0, key.length);
			} else {
				WSE.getLogger().severe("FIXED KEYLENGTH WAS NOT LONG ENOUGH: " + key.length + " > " + fixedKey.length);
				key = SHttp.genByteArray(bitLength);
			}
		} else {
			key = SHttp.genByteArray(bitLength);
		}
		return new SKey(name, key, SHTTP_SESSION_LIFE_LENGTH);
	}

	public static boolean hasExpired(SKey key) {
		if (key == null)
			return true;
		return key.hasExpired();
	}

	public static void setFixedKey(byte[] key) {
		fixedKey = key;
		if (key != null)
			WSE.getLogger().severe("USING FIXED SHTTP KEY!!");
	}
}
