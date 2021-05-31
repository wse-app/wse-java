package wse.utils.xml;

public class XMLHierarchyIterator {

	public static interface Receiver {
		public void receive(XMLElement element);
	}
	public static void iterate(Receiver receiver, XMLElement root) {
		if (receiver == null)
			return;
		send(receiver, root);
	}

	
	private static void send(Receiver receiver, XMLElement root) {
		receiver.receive(root);
		for (XMLElement c : root.getChildren())
			send(receiver, c);
	}

}
