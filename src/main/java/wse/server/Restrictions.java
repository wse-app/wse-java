package wse.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Restrictions {

//	255.255.0.0
//	private static final long LOCAL_NET_MASK = 0xFFFF0000;

	private int backlog = 50;
	private AcceptPolicy accept_policy = AcceptPolicy.ANY;

	private final List<String> accept_only = new ArrayList<>();
	private final List<String> ban_list = new ArrayList<>();

	private boolean clientAuthRequired = false;

	public boolean getNeedClientAuth() {
		return clientAuthRequired;
	}

	public Restrictions setNeedClientAuth(boolean clientAuthRequired) {
		this.clientAuthRequired = clientAuthRequired;
		return this;
	}

	/*
	 * Accept Policy
	 */

	public Restrictions setAcceptPolicy(AcceptPolicy policy) {
		if (policy == null)
			this.accept_policy = AcceptPolicy.ANY;
		else
			this.accept_policy = policy;
		return this;
	}

	public AcceptPolicy getAcceptPolicy() {
		return this.accept_policy;
	}

	/*
	 * LAN Options
	 */

	public Restrictions addAcceptOnly(String... host) {
		Collections.addAll(accept_only, host);
		return this;
	}

	public void clearAcceptOnly() {
		accept_only.clear();
	}

	public Iterable<String> getAcceptOnly() {
		return accept_only;
	}

	public boolean isSingleAccept() {

		return this.accept_policy == AcceptPolicy.LAN && accept_only.size() == 1;
	}

	public String getAcceptHost() {
		if (isSingleAccept())
			return accept_only.get(0);
		return null;
	}

	/*
	 * Ban-List
	 */

	public Restrictions addBannedHost(String... host) {
		Collections.addAll(ban_list, host);
		return this;
	}

	public void clearBanList() {
		accept_only.clear();
	}

	public Iterable<String> getBanList() {
		return accept_only;
	}

	/*
	 * Backlog options
	 */

	public int getBacklog() {
		return this.backlog;
	}

	public Restrictions setBacklog(int backlog) {
		this.backlog = backlog;
		return this;
	}

	// STATIC

	public static Restrictions empty() {
		return new Restrictions();
	}

//	private static final boolean isSameLocalNet(long ip1, long ip2) {
//		return ((LOCAL_NET_MASK & ip1) == (LOCAL_NET_MASK & ip2));
//	}

//	private static final boolean isLocalNet(long ip1) {
//		InetAddress inetAddress;
//		try {
//			inetAddress = InetAddress.getLocalHost();
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			return false;
//		}
//
//		long ip2 = getIp(inetAddress.getHostAddress());
//
//		return isSameLocalNet(ip1, ip2);
//	}

//	private static final boolean isLocalNet(String ip) {
//		return isLocalNet(getIp(ip));
//	}

//	private static final long getIp(String ip) {
//		String[] parts = ip.split("[.]");
//
//		long i0 = Long.parseLong(parts[0]);
//		long i1 = Long.parseLong(parts[1]);
//		long i2 = Long.parseLong(parts[2]);
//		long i3 = Long.parseLong(parts[3]);
//
//		long result = (i0 << 24) | (i1 << 16) | (i2 << 8) | (i3);
//		return result;
//	}

}
