package orwell.proxy.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumMessageType;

public class ZmqMessageDecoder {
    final static Logger logback = LoggerFactory.getLogger(ZmqMessageDecoder.class);
    private final String routingId;
    private final String typeString;
    private final EnumMessageType type;
    private final byte[] message;
    private final String zmqMessageString;

    public ZmqMessageDecoder(byte[] raw_zmq_message) {
        zmqMessageString = new String(raw_zmq_message);
        final String[] zmqMessageStringArray = zmqMessageString.split(" ");

        // raw_zmq_message format:
        // routingID typeString message
        //           ^          ^
        //           |          indexMessage
        //           indexType

        routingId = zmqMessageStringArray[0];
        typeString = zmqMessageStringArray[1];
        message = zmqMessageStringArray[2].getBytes();
        switch (typeString) {
            case "Registered":
                type = EnumMessageType.REGISTERED;
                break;
            case "Input":
                type = EnumMessageType.INPUT;
                break;
            case "GameState":
                type = EnumMessageType.GAME_STATE;
                break;
            case "ServerRobotState":
                type = EnumMessageType.SERVER_ROBOT_STATE;
                break;
            default:
                type = EnumMessageType.UNKNOWN;
                logback.warn("Message typeString unknown: " + typeString);
        }

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
