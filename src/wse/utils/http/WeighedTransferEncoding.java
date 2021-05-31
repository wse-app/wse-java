package wse.utils.http;

public final class WeighedTransferEncoding implements Comparable<WeighedTransferEncoding> {
	public final TransferEncoding encoding;
	public final double q;

	public WeighedTransferEncoding(TransferEncoding encoding) {
		this(encoding, 1);
	}

	public WeighedTransferEncoding(TransferEncoding encoding, double q) {
		this.encoding = encoding;
		this.q = q;
	}

	public static WeighedTransferEncoding fromText(String text) {

		if (!text.contains(";")) {
			TransferEncoding encoding = TransferEncoding.fromName(text);
			if (encoding == null)
				return null;
			return new WeighedTransferEncoding(encoding);
		}

		String[] parts = text.split(";");
		TransferEncoding encoding = null;

		if (parts != null && parts.length > 1) {
			encoding = TransferEncoding.fromName(parts[0]);
			if (encoding == null)
				return null;
		} else {
			encoding = TransferEncoding.fromName(text);
			if (encoding == null)
				return null;
			return new WeighedTransferEncoding(encoding);
		}
		double q_ = 0;

		for (int i = 1; i < parts.length; i++) {
			if (parts[i] == null)
				continue;
			String part = parts[i].trim();
			String value = part.substring(2);
			char f = part.charAt(0);

			switch (f) {
			case 'q':
				try {
					q_ = Double.parseDouble(value);
				} catch (Exception e) {
				}
				break;
			default:
				break;
			}
		}

		return new WeighedTransferEncoding(encoding, q_);
	}

	@Override
	public int compareTo(WeighedTransferEncoding o) {
		if (o == null)
			return 1;

		if (o.q > this.q)
			return -1;
		if (o.q < this.q)
			return 1;

		return 0;
	}
}
