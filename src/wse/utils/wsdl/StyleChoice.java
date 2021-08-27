package wse.utils.wsdl;

/**
 * For RPC-oriented messages, each part is a parameter or a return value and
 * appears inside a wrapper element within the body. <br>
 * <br>
 * For document-oriented messages, there are no additional wrappers, so the
 * message parts appear directly under the SOAP body element.
 * 
 * @author WSE
 *
 */

public enum StyleChoice {
	RPC, DOCUMENT;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
