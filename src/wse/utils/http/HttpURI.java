package wse.utils.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import wse.WSE;
import wse.utils.internal.PrettyPrinter;
import wse.utils.internal.StringGatherer;
import wse.utils.writable.StreamWriter;

public class HttpURI implements StreamWriter, PrettyPrinter {

	private String path;
	private Map<String, String> query = new LinkedHashMap<String, String>();
	private String fragment;

	private HttpURI() {
	}

	public HttpURI(HttpURI copy) {
		if (copy == null)
			throw new NullPointerException("Can't copy null object");
		this.path = copy.path;
		this.fragment = copy.fragment;
		for (Entry<String, String> e : copy.query.entrySet()) {
			this.query.put(e.getKey(), e.getValue());
		}
	}

	public static HttpURI raw(String raw) {
		HttpURI u = new HttpURI();
		u.path = raw;
		return u;
	}

	public static HttpURI fromURI(String uri) {
		if (uri == null)
			return null;
		URI u = URI.create(uri);
		return fromURI(u);
	}

	public static HttpURI fromURI(URI uri) {
		if (uri == null)
			return null;
		HttpURI result = new HttpURI();
		result.fragment = uri.getFragment();

		if (uri.getQuery() != null) {
			String[] params = uri.getRawQuery().split("[&]");

			for (String param : params) {
				String[] k_v = param.split("=", 2);
				if (k_v != null && k_v.length == 2) {
					result.setQuery(WSE.urlDecode(k_v[0]), WSE.urlDecode(k_v[1]));
				}
			}
		}

		result.path = uri.getPath();
		if (result.path == null) {
			result.path = "/";
		} else if (result.path.trim().isEmpty()) {
			result.path = "/";
		}
		return result;
	}

	public String getQuery(String name) {
		return query.get(name);
	}

	public void setQuery(String name, String value) {
		if (name.isEmpty())
			throw new IllegalArgumentException("Can't have empty names/values in parameters");

		query.put(name, value);
	}

	public Set<String> getQueryNames() {
		return query.keySet();
	}

	public Set<Entry<String, String>> getQuery() {
		return query.entrySet();
	}

	@Override
	public void writeToStream(OutputStream stream, Charset charset) throws IOException {
		if (path != null) {
			stream.write(path.getBytes());
		}

		if (query.size() > 0) {
			int i = 0;
			for (Entry<String, String> e : query.entrySet()) {
				if (i == 0)
					stream.write('?');
				else
					stream.write('&');
				stream.write(e.getKey().getBytes());
				stream.write('=');
				stream.write(e.getValue().getBytes());
			}
		}

		if (fragment != null) {
			stream.write('#');
			stream.write(fragment.getBytes());
		}
	}

	@Override
	public String toString() {
		return new String(toByteArray());
	}

	public int length() {
		int result = 0;

		if (path != null)
			result += path.length();

		if (query.size() > 0) {
			for (Entry<String, String> e : query.entrySet()) {

				result++; // ?|&

				result += WSE.urlEncode(e.getKey()).getBytes().length;
				result++; // =
				result += WSE.urlEncode(e.getValue()).getBytes().length;

			}
		}

		if (fragment != null)
			result += 1 + WSE.urlEncode(fragment).getBytes().length;
		return result;
	}

	public byte[] toByteArray() {
		byte[] b = new byte[length()];
		write(b, 0);
		return b;
	}

	public int write(byte[] dest, int off) {
		int p = off;

		if (path != null) {
			System.arraycopy(path.getBytes(), 0, dest, p, path.length());
			p += path.length();
		}

		byte[] key, value, frag;
		if (query.size() > 0) {
			int i = 0;
			for (Entry<String, String> e : query.entrySet()) {
				if (i == 0)
					dest[p++] = '?';
				else
					dest[p++] = '&';
				key = WSE.urlEncode(e.getKey()).getBytes();
				value = WSE.urlEncode(e.getValue()).getBytes();
				System.arraycopy(key, 0, dest, p, key.length);
				p += key.length;
				dest[p++] = '=';
				System.arraycopy(value, 0, dest, p, value.length);
				p += value.length;
				i++;
			}
		}

		if (fragment != null) {
			dest[p++] = '#';
			frag = WSE.urlEncode(fragment).getBytes();
			System.arraycopy(frag, 0, dest, p, frag.length);
			p += frag.length;
		}
		return p - off;
	}

	public String getPath() {
		return path;
	}

	public String getFragment() {
		return fragment;
	}

	public URI toURI() {
		try {
			return new URI(toString());
		} catch (URISyntaxException e) {
			return null;
		}
	}

	@Override
	public StringGatherer prettyPrint() {
		return prettyPrint(0);
	}

	@Override
	public StringGatherer prettyPrint(int level) {
		StringGatherer builder = new StringGatherer();
		prettyPrint(builder, level);
		return builder;
	}

	@Override
	public void prettyPrint(StringGatherer builder, int level) {
		// TODO Auto-generated method stub

	}
}
