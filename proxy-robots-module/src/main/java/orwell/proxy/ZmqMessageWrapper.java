package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZmqMessageWrapper {
	final static Logger logback = LoggerFactory.getLogger(ZmqMessageWrapper.class); 

	public String routingId;
	private String typeString;
    public EnumMessageType type;
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
		// routingID typeString message
		//           ^    ^
		//           |    indexMessage
		//           indexType
		int lengthRoutingID = indexType - 1;
		int lengthType = indexMessage - indexType - 1;
		routingId = new String(raw_zmq_message, 0, lengthRoutingID);
		typeString = new String(raw_zmq_message, indexType, lengthType);
        switch(typeString) {
            case "Registered":
                type = EnumMessageType.REGISTERED;
                break;
            case "Input":
                type = EnumMessageType.INPUT;
                break;
            case "GameState":
                type = EnumMessageType.GAMESTATE;
                break;
            case "ServerRobotState":
                type = EnumMessageType.SERVER_ROBOT_STATE;
                break;
            default:
                type = EnumMessageType.UNKNOWN;
                logback.warn("Message typeString unknown: " + typeString);
        }
		int lengthMessage = raw_zmq_message.length - lengthType
				- lengthRoutingID - 2;
		message = new byte[lengthMessage];
		System.arraycopy(raw_zmq_message, indexMessage, message, 0,
				message.length);

		System.out.flush();
		logback.info("Message received: " + zmqMessageString);

		logback.info("Message [TYPE]: " + type);
	}
}
