package wse.utils;

public enum Protocol
{
	HTTP("http"), HTTPS("https"), SHTTP("shttp"), WEB_SOCKET("ws"), WEB_SOCKET_SECURE("wss");

	private String nicename;

	Protocol(String nicename)
	{
		this.nicename = nicename;
	}

	public String toString()
	{
		return nicename;
	}
	
	public boolean isWebSocket() {
		return this == WEB_SOCKET || this == WEB_SOCKET_SECURE;
	}
	
	public boolean isSecure() {
		return this == HTTPS || this == SHTTP || this == WEB_SOCKET_SECURE;
	}
	
	public Integer getDefaultPort() {
		if (this == HTTP) return 80;
		if (this == HTTPS) return 443;
		return null;
	}

	public static Protocol parseIgnoreCase(String nicename)
	{
		for (Protocol t : Protocol.values())
		{
			if (t.nicename.equalsIgnoreCase(nicename))
				return t;
		}
		return null;
	}
	
	
	public static Protocol parse(String nicename)
	{
		for (Protocol t : Protocol.values())
		{
			if (t.nicename.equals(nicename))
				return t;
		}
		return null;
	}
}
