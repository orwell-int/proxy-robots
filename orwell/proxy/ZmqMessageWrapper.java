package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZmqMessageWrapper {
	final static Logger logback = LoggerFactory.getLogger(ZmqMessageWrapper.class); 

	public String routingId;
	public String type;
	public byte[] message;
	public static final byte SPACE_CODE = 32; // ascii code of SPACE character
	public String zmqMessageString;
	
	public ZmqMessageWrapper(byte[] raw_zmq_message) {
		zmqMessageString = new String(raw_zmq_message);
		int indexType = 0;
		int indexMessage = 0;
		int index = 0;
		for (byte item : raw_zmq_message) {
			if (0 == indexType) {
				if (SPACE_CODE == item) {
					indexType = index + 1;
				}
			} else {
				if (SPACE_CODE == item) {
					indexMessage = index + 1;
					break;
				}
			}
			++index;
		}
		// routingID type message
		// ^ ^
		// | indexMessage
		// indexType
		int lengthRoutingID = indexType - 1;
		int lengthType = indexMessage - indexType - 1;
		this.routingId = new String(raw_zmq_message, 0, lengthRoutingID);
		String type = new String(raw_zmq_message, indexType, lengthType);
		int lengthMessage = raw_zmq_message.length - lengthType
				- lengthRoutingID - 2;
		byte[] message = new byte[lengthMessage];
		System.arraycopy(raw_zmq_message, indexMessage, message, 0,
				message.length);

		System.out.flush();
		logback.info("Message received: " + zmqMessageString);

		//proxyRobots.tank.setRoutingID(routingID);
		logback.info("Message [TYPE]: " + type);
	}
}
