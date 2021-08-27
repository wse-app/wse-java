package wse.utils.shttp;

import java.util.Arrays;

public class SKey {

	private final String keyname;
	private final byte[] key;

	private final String host;
	private final int port;

	private final int reachPort;

	private final long expiryTime;

	private final int blockSize = 16;
	private final int injectionSize = 1;

	/**
	 * Total life length in seconds
	 */
	private final long lifeLength;

	public SKey(String keyname, byte[] key, long secondsTilExpire) {
		this(keyname, key, null, -1, -1, secondsTilExpire);
	}

	public SKey(String keyname, byte[] key, String host, int port, int reachPort, long secondsTilExpire) {
		this.keyname = keyname;
		this.key = key;
		this.host = host;
		this.port = port;
		this.reachPort = reachPort;
		this.lifeLength = secondsTilExpire;

		long millisExpire = secondsTilExpire * 1000;
		this.expiryTime = System.currentTimeMillis() + millisExpire;
	}

	public boolean hasExpired() {
		return (System.currentTimeMillis() - expiryTime) > 0;
	}

	public String getKeyName() {
		return keyname;
	}

	public String getInitHost() {
		return host;
	}

	public int getInitPort() {
		return port;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getInjectionSize() {
		return injectionSize;
	}

	public long getExpire() {
		return expiryTime;
	}

	public long getLifeLength() {
		return lifeLength;
	}

	public int getReachPort() {
		return reachPort;
	}

	public byte[] getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "SKey [\n\tkeyname=" + keyname + ",\n\tkey=" + Arrays.toString(key) + ",\n\thost=" + host + ", \n\tport="
				+ port + ", \n\treachPort=" + reachPort + ", \n\texpiryTime=" + expiryTime + ", \n\tblockSize="
				+ blockSize + ", \n\tinjectionSize=" + injectionSize + ", \n\tlifeLength=" + lifeLength + "}\n]";
	}

}
