package wse.server.servlet.soap;

import wse.utils.MimeType;
import wse.utils.exception.SoapFault;
import wse.utils.http.HttpAttributeList;
import wse.utils.http.StreamUtils;
import wse.utils.json.JObject;

public class JSONSoapFaultErrorFormatter extends SoapFaultErrorFormatter {

	public byte[] error(SoapFault fault, HttpAttributeList attributes) {
		JObject faultObj = new JObject();
		fault.create(faultObj);

		JObject response = new JObject();
		response.put("Fault", faultObj);

		attributes.setContentType(MimeType.application.json);

		return StreamUtils.catchAll(response, response.preferredCharset());
	}

}
