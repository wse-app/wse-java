package wse.utils.http;

import wse.utils.HttpCodes;
import wse.utils.internal.StringGatherer;

public class HttpStatusLine extends HttpDescriptionLine{
	
	private String s_code;
	private int code;
	
	private String statusMessage;
	
	public HttpStatusLine(HttpStatusLine copy) {
		if (copy == null)
			return;
		this.s_code = copy.s_code;
		this.code = copy.code;
		this.statusMessage = copy.statusMessage;
	}
	
	public HttpStatusLine(int code) {
		super();
		setStatusCode(code);
	}

	
	private HttpStatusLine(String httpVersion, String code, String statusMessage) {
		super(httpVersion);
		setStatusCode(code);
		this.statusMessage = statusMessage.trim();
	}
	
	public static HttpStatusLine fromString(String requestLine)
	{
		if (requestLine == null)
			return null;
		
		String[] parts = requestLine.split(" ", 3);
		if (parts == null || parts.length < 2)
			return null;
		
		return new HttpStatusLine(parts[0], parts[1], parts.length == 3 ? parts[2] : null);
	}
	
	public static HttpStatusLine fromCode(int code)
	{	
		return new HttpStatusLine(code);
	}
	
	@Override
	public void prettyPrint(StringGatherer builder, int level) {
		builder.add(httpVersion);
		builder.add(" ");
		builder.add(s_code);
		builder.add(" ");
		builder.add(statusMessage);
	}
	
	@Override
	public String toString() {
		return new String(toByteArray());
	}
	
	public int length()
	{
		return httpVersion.length() + s_code.length() + statusMessage.length() + 2;
	}
	
	public byte[] toByteArray()
	{
		byte[] b = new byte[length()];
		write(b, 0);
		return b;
	}
	
	public int write(byte[] dest, int off)
	{
		int p = off;
		System.arraycopy(httpVersion.getBytes(), 0, dest, p, httpVersion.length());
		p += httpVersion.length();
		dest[p++] = ' ';
		System.arraycopy(s_code.getBytes(), 0, dest, p, s_code.length());
		p += s_code.length();
		dest[p++] = ' ';
		System.arraycopy(statusMessage.getBytes(), 0, dest, p, statusMessage.length());
		p += statusMessage.length();
		return p - off;
	}
	
	public String getStatusMessage() {
		return this.statusMessage;
	}
	
	public void setHttpVersion(String version)
	{
		this.httpVersion = version;
	}
	
	public int getStatusCode()
	{
		return code;
	}
	
	public void setStatusCode(int code)
	{
		this.code = code;
		this.s_code = String.valueOf(code);
		updateMessage(this.code);
	}
	
	public void setStatusCode(String code)
	{
		code = code.trim();
		if (!code.matches("[1-9][0-9][0-9]")) {
			throw new IllegalArgumentException("Invalid Http status code: " + code);
		}
		
		this.code = Integer.parseInt(code);
		this.s_code = code;
		updateMessage(this.code);
	}
	
	private void updateMessage(int code)
	{
		if (HttpCodes.validCodeMessage(code))
			this.statusMessage = HttpCodes.getCodeMessage(code);
		else
			this.statusMessage = "Custom-Status";
		
	}
	
	public void setCustomStatusMessage(String customStatusMessage)
	{
		
		if (customStatusMessage == null || customStatusMessage.trim().isEmpty())
			this.statusMessage = "Custom-Status";
		else
			this.statusMessage = customStatusMessage;
	}
	
	public boolean isSuccessCode()
	{
		return HttpCodes.isSuccessCode(code);
	}
	
}
