package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZmqMessageDecoder {
    final static Logger logback = LoggerFactory.getLogger(ZmqMessageDecoder.class);
    private static final byte SPACE_CODE = 32; // ascii code of SPACE character
    private final String routingId;
    private final String typeString;
    private final EnumMessageType type;
    private final byte[] message;
    private final String zmqMessageString;

    public ZmqMessageDecoder(byte[] raw_zmq_message) {
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
        lengthRoutingID = (lengthRoutingID < 0) ? 0 : lengthRoutingID;
        int lengthType = indexMessage - indexType - 1;
        lengthType = (lengthType < 0) ? 0 : lengthType;
        routingId = new String(raw_zmq_message, 0, lengthRoutingID);
        typeString = new String(raw_zmq_message, indexType, lengthType);
        switch (typeString) {
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
        lengthMessage = (lengthMessage < 0) ? 0 : lengthMessage;
        message = new byte[lengthMessage];
        System.arraycopy(raw_zmq_message, indexMessage, message, 0,
                message.length);

        System.out.flush();

        logback.info("Message received: [RoutingID] " + routingId + " [TYPE]: " + type);
    }

    public String getZmqMessageString() {
        return zmqMessageString;
    }

    public String getRoutingId() {
        return routingId;
    }

    public EnumMessageType getMessageType() {
        return type;
    }

    public byte[] getMessageBytes() {
        return message;
    }
}
