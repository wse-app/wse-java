package wse.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import wse.server.servlet.HttpServletRequest;
import wse.server.servlet.HttpServletResponse;
import wse.server.shttp.SHttpServerSessionStore;
import wse.utils.HttpCodes;
import wse.utils.HttpResult;
import wse.utils.MimeType;
import wse.utils.SHttp;
import wse.utils.exception.SHttpException;
import wse.utils.http.HttpBuilder;
import wse.utils.http.StreamUtils;
import wse.utils.shttp.SKey;
import wse.utils.stream.LayeredOutputStream;
import wse.utils.stream.RecordingInputStream;
import wse.utils.stream.RecordingOutputStream;
import wse.utils.stream.WseInputStream;
import wse.utils.writable.StreamCatcher;

/**
 * 
 * The shttp decryptor
 * 
 * @author WSE
 *
 */
public class ServiceReceiverSHttp extends ServiceReceiverHttp {

	private SHttpServerSessionStore storeRef;

	public ServiceReceiverSHttp(ServiceManager manager, int port, Restrictions restrictions) {
		super(manager, port, restrictions);
	}

	public void setSHttpSessionStore(SHttpServerSessionStore store) {
		this.storeRef = store;
	}

	public SHttpServerSessionStore getSHttpSessionStore() {
		return this.storeRef;
	}

	@Override
	public String getProtocol() {
		return "sHttp";
	}

	@Override
	public void treatCall(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String keyName = request.getAttributeValue(SHttp.KEY_NAME_ATTRIBUTE);

		if (keyName == null) {
			response.sendError(HttpCodes.BAD_REQUEST);
			return;
		}

		keyName = parseKey(keyName);
		if (keyName == null) {
			response.sendError(HttpCodes.BAD_REQUEST);
			return;
		}

		SKey key = storeRef.getKey(keyName);

		if (key == null) {
			response.sendError(HttpCodes.SECURITY_RETRY);
			return;
		}

		if (key.hasExpired()) {
			response.sendError(HttpCodes.SECURITY_RETRY);
			return;
		}

		InputStream input = request.getContent();
		if (SHttp.LOG_ENCRYPTED_DATA)
			input = new RecordingInputStream(input, log, Level.FINEST, "SHttp-Encrypted Input Content:", true);

		WseInputStream decryptedInput = SHttp.sHttpDecryptData(input, key);
		input.close();
		if (decryptedInput == null)
			throw new SHttpException("Failed to decrypt message");

		decryptedInput = new RecordingInputStream(decryptedInput, log, Level.FINEST, "Request Content:");

		HttpResult decrypted = HttpBuilder.read(decryptedInput, true);
		decryptedInput.close();

		HttpServletRequest decryptedRequest = HttpServletRequest.make(decrypted.getHeader(), request.getRequestInfo(),
				decrypted.getContent());

		// Create new response
		StreamCatcher catcher = new StreamCatcher();
		LayeredOutputStream output = new LayeredOutputStream(catcher);
		output.then(new RecordingOutputStream(log, Level.FINEST, "Response Content:"));
		output.sHttpEncrypt(key);
		if (SHttp.LOG_ENCRYPTED_DATA)
			output.record(log, Level.FINEST, "SHttp-Encrypted Response Content:", true);

		HttpServletResponse decryptedResponse = new HttpServletResponse(output);
		super.treatCall(decryptedRequest, decryptedResponse);

		output.flush();
		output.close();

		response.getHttpHeader().setDescriptionLine(SHttp.makeStatusLine(200));
		response.setContentLength(catcher.getSize());
		response.setContentType(MimeType.message.http);
		response.setAttribute("Prearranged-Key-Info", "outband:" + key.getKeyName());

		StreamUtils.write(catcher.asInputStream(), response, catcher.getSize());

		response.flush();
		response.close();
	}

	private String parseKey(String attribValue) {
		// outband:keyname

		if (!attribValue.startsWith("outband:"))
			return null;

		String[] parts = attribValue.split(":", 2);
		if (parts == null || parts.length != 2)
			return null;

		return parts[1];
	}

}
