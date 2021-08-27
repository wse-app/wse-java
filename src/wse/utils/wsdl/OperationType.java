package wse.utils.wsdl;

public enum OperationType {
	/** input, output */
	RequestResponse,
	/** input */
	OneWay,
	/** output, input */
	SolicitResponse,
	/** output */
	Notification;

	public boolean clientInitiated() {
		return this == RequestResponse || this == OneWay;
	}
}