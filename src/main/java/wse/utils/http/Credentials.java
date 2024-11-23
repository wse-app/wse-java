package wse.utils.http;

import java.nio.charset.Charset;
import java.util.Arrays;

import wse.WSE;
import wse.utils.ArrayUtils;

public class Credentials {
	public static final String HEADER_ATTRIB_KEY = "Authorization";

	public final byte[] user_id, password;

	public Credentials(String user_id, String password) {
		this(user_id.getBytes(Charset.forName("UTF-8")), password.getBytes(Charset.forName("UTF-8")));
	}

	public Credentials(byte[] user_id, byte[] password) {
		this.user_id = user_id;
		this.password = password;
	}

	public String toString() {
		return new String(user_id) + ":" + new String(password);
	}

	public String getUserID() {
		return new String(user_id);
	}

	public String getPassword() {
		return new String(password);
	}

	public byte[] toByteArray() {
		byte[] result = new byte[user_id.length + password.length + 1];
		System.arraycopy(user_id, 0, result, 0, user_id.length);
		result[user_id.length] = ':';
		System.arraycopy(password, 0, result, user_id.length + 1, password.length);
		return result;

	}

	public static Credentials fromHeader(HttpAttributeList header) {
		HeaderAttribute attrib = header.getAttribute(HEADER_ATTRIB_KEY);
		if (attrib == null)
			return null;

		String value = attrib.value;
		if (value == null)
			return null;

		String[] split = value.split(" ");
		if (split.length < 2)
			return null;
		String cred = split[1].trim();
		byte[] array = WSE.parseBase64Binary(cred);
		byte[][] val = ArrayUtils.split(array, (byte) ':', 2, true);
		if (val.length != 2) {
			return null;
		}
		return new Credentials(val[0], val[1]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(password);
		result = prime * result + Arrays.hashCode(user_id);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Credentials other = (Credentials) obj;
		if (!Arrays.equals(password, other.password))
			return false;
		if (!Arrays.equals(user_id, other.user_id))
			return false;
		return true;
	}

}
