package wse.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class Target {

	private final static URI temp_uri;
	static {
		URI temp = null;
		try {
			temp = new URI("http", null, "temp.uri", 80, null, null, null);
		} catch (URISyntaxException e) {
		}
		temp_uri = temp;
	}

	private URI target = temp_uri;

	public Target() {
	}

	public Target(Target copy) {
		this.target = copy.target;
	}

	public Target(String uri) throws URISyntaxException {
		setTarget(uri);
	}

	public Target(URI uri) {
		setTarget(uri);
	}

	public Target(String scheme, String host, int port, String path) {
		setTarget(scheme, host, port, path);
	}

	private void notNullTarget() {
		if (target == null) {
			target = temp_uri;
		}
	}

	public URI getTargetURI() {
		return target;
	}

	public void setTarget(Protocol protocol, String host, int port, String path) {
		setTarget(protocol.toString(), host, port, path);
	}

	public void setTarget(String scheme, String host, int port, String path) {
		try {
			setTarget(new URI(scheme, target.getUserInfo(), host, port, path, target.getQuery(), target.getFragment()));
		} catch (URISyntaxException e) {
			notNullTarget();
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTarget(String scheme, String host, int port) {
		try {
			setTarget(new URI(scheme, target.getUserInfo(), host, port, target.getPath(), target.getQuery(),
					target.getFragment()));
		} catch (URISyntaxException e) {
			notNullTarget();
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTarget(String host, int port) {
		try {
			setTarget(new URI(target.getScheme(), target.getUserInfo(), host, port, target.getPath(), target.getQuery(),
					target.getFragment()));
		} catch (URISyntaxException e) {
			notNullTarget();
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTarget(String uri) throws URISyntaxException {
		try {
			setTarget(new URI(uri));
		} catch (URISyntaxException e) {
			notNullTarget();
			throw e;
		}
	}

	public void setTarget(URI uri) {
		this.target = uri;
		notNullTarget();
	}

	public void setTargetScheme(Protocol protocol) {
		setTargetScheme(protocol.toString());
	}

	public void setTargetScheme(String scheme) {
		try {
			setTarget(new URI(scheme, target.getUserInfo(), target.getHost(), target.getPort(), target.getPath(),
					target.getQuery(), target.getFragment()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTargetUserInfo(String userinfo) {
		try {
			setTarget(new URI(target.getScheme(), userinfo, target.getHost(), target.getPort(), target.getPath(),
					target.getQuery(), target.getFragment()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTargetHost(String host) {
		try {
			setTarget(new URI(target.getScheme(), target.getUserInfo(), host, target.getPort(), target.getPath(),
					target.getQuery(), target.getFragment()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTargetPort(int port) {
		try {
			setTarget(new URI(target.getScheme(), target.getUserInfo(), target.getHost(), port, target.getPath(),
					target.getQuery(), target.getFragment()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTargetPath(String path) {
		try {
			setTarget(new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(), path,
					target.getQuery(), target.getFragment()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTargetQuery(String query) {
		try {
			setTarget(new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(),
					target.getPath(), query, target.getFragment()));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	public void setTargetFragment(String fragment) {
		try {
			setTarget(new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(),
					target.getPath(), target.getQuery(), fragment));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Invalid URI: " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @return The scheme part of the target uri
	 */
	public URI getTarget() {
		return target;
	}

	/**
	 * 
	 * @return The scheme part of the target uri
	 */
	public String getTargetScheme() {
		return target != null ? target.getScheme() : null;
	}

	/**
	 * 
	 * @return The scheme part of the target uri
	 */
	public Protocol getTargetSchemeAsProtocol() {
		return Protocol.forName(getTargetScheme());
	}

	/**
	 * 
	 * @return The host part of the target uri
	 */
	public String getTargetHost() {
		return target != null ? target.getHost() : null;
	}

	/**
	 * 
	 * @return The port of the target uri
	 */
	public int getTargetPort() {
		return target != null ? target.getPort() : -1;
	}

	/**
	 * 
	 * @return The path of the target uri
	 */
	public String getTargetPath() {
		return target != null ? target.getPath() : null;
	}

	/**
	 * 
	 * @return The query part of the target uri
	 */
	public String getTargetQuery() {
		return target != null ? target.getQuery() : null;
	}

	/**
	 * 
	 * @return The fragment part of the target uri
	 */
	public String getTargetFragment() {
		return target != null ? target.getFragment() : null;
	}

}
