package wse.utils.http;

public enum TransferEncoding {

	CHUNKED(true, "chunked"),

	IDENTITY(true, "identity"),

	COMPRESS(false, "compress"),

	DEFLATE(false, "deflate"),

	BR(false, "br"),

	GZIP(false, "gzip", "x-gzip");

	public static final String KEY = "Transfer-Encoding";

	public final String name;
	public final String[] alias;
	public final boolean supported;

	TransferEncoding(boolean supported, String name, String... alias) {
		this.name = name;
		this.alias = alias;
		this.supported = supported;
	}

	public static TransferEncoding fromName(String name) {
		if (name == null)
			return null;
		for (TransferEncoding te : values()) {
			if (te.name.equals(name)) {
				return te;
			}

			if (te.alias != null) {
				for (String s : te.alias)
					if (s.equals(name))
						return te;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

}
