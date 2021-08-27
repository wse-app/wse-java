package wse.utils.http;

public final class WeighedString implements Comparable<WeighedString> {
	public final String string;
	public final Double q;
	public final String v;

	public WeighedString(String text) {
		if (!text.contains(";")) {
			this.string = text;
			this.q = 1d;
			this.v = null;
			return;
		}

		String[] parts = text.split(";");

		if (parts != null && parts.length > 1) {
			this.string = parts[0];
		} else {
			this.string = text;
			this.q = 1d;
			this.v = null;
			return;
		}

		double q_ = 0;
		String v_ = null;

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
			case 'v':
				v_ = value;
				break;
			default:
				break;
			}
		}

		this.q = q_;
		this.v = v_;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(string);

		return sb.toString();
	}

	@Override
	public int compareTo(WeighedString o) {
		if (o == null)
			return 1;

		if (o.q > this.q)
			return -1;
		if (o.q < this.q)
			return 1;

		return 0;
	}
}
