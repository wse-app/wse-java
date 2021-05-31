package wse.utils.ssl;

import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * 
 * Always allows server hostname if address is an ip address.
 * 
 * @author WSE
 *
 */
public class WSETrustManager implements X509TrustManager, HostnameVerifier{
	
	private final X509TrustManager tm;
	
	private Map<Thread, String> expectedHost = Collections.synchronizedMap(new HashMap<Thread, String>());
	
	public static final String IP_REGEX = "[0-9]*.[0-9]*.[0-9]*.[0-9]*";

	public WSETrustManager(X509TrustManager tm) {
		this.tm = tm;
	}

	public void setExpectedHost(String host) {
		this.expectedHost.put(Thread.currentThread(), host);
	}

	public X509Certificate[] getAcceptedIssuers() {
		return tm.getAcceptedIssuers();
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		tm.checkClientTrusted(chain, authType);
	}

	// https://gist.github.com/chiuki/fd581a52ecc51fb9ed7e447d083f92cc
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		tm.checkServerTrusted(chain, authType);
		
		verifyHostname(chain[0], this.expectedHost.remove(Thread.currentThread()));
	}
	
	

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
	
	public static void verifyHostname(X509Certificate certificate, String expected_hostname) throws CertificateException
	{
		if (expected_hostname == null || "localhost".equals(expected_hostname) || expected_hostname.matches(WSETrustManager.IP_REGEX)) {
			return;
		}
		
		String server_host = getCommonName(certificate);
		
		if (matchHostname(server_host, expected_hostname))
			return;
		
		// Does not match server hostname, check alternatives
		List<String> alternatives = getSubjectAlternativeNames(certificate);
		
		for (String alias : alternatives)
		{
			if (matchHostname(alias, expected_hostname))
				return;
		}
		
		throw new CertificateException("Neither server hostname \"" + server_host + "\" nor alias" + (alternatives.size() == 1 ? " " : "es ") + alternatives.toString() + " matched expected hostname \"" + expected_hostname + "\"");
	}
	
	public static boolean matchHostname(String hostname, String expected)
	{
//		System.out.println("Trying to match: hostname: " + hostname + ", expected: " + expected);
		if (hostname == null || expected == null)
			return false;
		
		if (expected.equals(hostname))
			return true;
		
		if (hostname.startsWith("*"))
		{
			hostname = hostname.substring(1);
			if (expected.endsWith(hostname))
				return true;
		}
		return false;
	}
	
	public static String getCommonName(X509Certificate certificate)
	{
		String name = certificate.getSubjectDN().getName();
		if (name != null && !name.isEmpty())
		{
//			System.out.println(name);
			int cn = name.indexOf("CN=");
			
			int c = name.indexOf(",", cn + 3);
			if (c != -1) {
				return name.substring(cn + 3, c);
			}
			
			return name.substring(cn + 3);
		}
		return null;
	}
	
	public static List<String> getSubjectAlternativeNames(X509Certificate certificate) {
		List<String> elements = new ArrayList<>();
	    try {
	        Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
	        if (altNames != null) {
	            for (List<?> altName : altNames) {
	                Integer altNameType = (Integer) altName.get(0);
	                if (altNameType != 2 && altNameType != 7) // dns or ip
	                    continue;
	                elements.add((String) altName.get(1));
	            }
	        }
	    } catch (CertificateParsingException ignored) {
	    }
	    return elements;
    }
}
