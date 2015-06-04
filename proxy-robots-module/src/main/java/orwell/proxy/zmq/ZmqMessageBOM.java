package orwell.proxy.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumMessageType;
import orwell.proxy.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Michaël Ludmann on 5/6/15.
 */
public class ZmqMessageBOM {
    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageBOM.class);
    private final static byte ZMQ_SEPARATOR = " ".getBytes()[0];
    private final EnumMessageType messageType;
    private final String routingId;
    private byte[] messageBodyBytes;

    public ZmqMessageBOM(final String routingId, final EnumMessageType messageType,
                         final byte[] messageBodyBytes) {
        this.messageType = messageType;
        this.routingId = routingId;
        this.messageBodyBytes = messageBodyBytes;
    }

    /**
     * @param raw_zmq_message Message receive through zmq protocol, with
     *                        the following format (split by spaces):
     *                        routingId typeString message
     *                        Careful: message is arbitrary binary data
     */
    public static ZmqMessageBOM parseFrom(final byte[] raw_zmq_message) throws ParseException {
        // We do not want to create a String from arbitrary binary data, so we
        // first isolate the 3 parts of the raw zmq message
        final List<byte[]> zmqMessageBytesList = Utils.split(ZMQ_SEPARATOR, raw_zmq_message, 3);

        if (3 != zmqMessageBytesList.size()) {
            logback.warn("ZmqMessage failed to split incoming message, missing items: " +
                    raw_zmq_message);
            throw new ParseException("Message does not contain all three mandatory items", 3);
        }

        // routingId was a string encoded in bytes, there is no issue to build a String from it
        final String routingId = new String(zmqMessageBytesList.get(0));
        // typeString was a string encoded in bytes, there is no issue to build a String from it
        final String typeString = new String(zmqMessageBytesList.get(1));
        // message is binary data, so we keep it as a byte array
        final byte[] messageBodyBytes = zmqMessageBytesList.get(2);

        final EnumMessageType type = getEnumTypeFromTypeString(typeString);

        logback.info("Message parsed: [RoutingID] " + routingId + " [TYPE]: " + type);
        return new ZmqMessageBOM(routingId, type, messageBodyBytes);
    }

    private static EnumMessageType getEnumTypeFromTypeString(final String typeString) {
        switch (typeString) {
            case "Registered":
                return EnumMessageType.REGISTERED;
            case "Input":
                return EnumMessageType.INPUT;
            case "GameState":
                return EnumMessageType.GAME_STATE;
            case "ServerRobotState":
                return EnumMessageType.SERVER_ROBOT_STATE;
            default:
                logback.warn("Message typeString unknown: " + typeString);
                return EnumMessageType.UNKNOWN;
        }
    }

    public String getRoutingId() {
        return routingId;
    }

    public EnumMessageType getMessageType() {
        return messageType;
    }

    /**
     * @return the body of the message, i.e. a Google protocol buffer
     */
    public byte[] getMessageBodyBytes() {
        return messageBodyBytes;
    }

    /**
     * @return the whole Zmq message that can be sent as is
     */
    public byte[] getZmqMessageBytes() {
        final StringBuilder zmqMessageHeaderBuilder = new StringBuilder();
        zmqMessageHeaderBuilder.append(routingId).append(" ");

        switch (messageType) {
            case REGISTER:
                zmqMessageHeaderBuilder.append("Register ");
                break;
            case SERVER_ROBOT_STATE:
                zmqMessageHeaderBuilder.append("ServerRobotState ");
                break;
            default:

        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(zmqMessageHeaderBuilder.toString().getBytes());
            outputStream.write(messageBodyBytes);
        } catch (final IOException e) {
            logback.error("Error while building zmqMessage " + e.getMessage());
            return null;
        }
        return outputStream.toByteArray();
    }

    /**
     * @return true is body or routingId or type is empty
     */
    public boolean isEmpty() {
        return (null == messageBodyBytes || 0 == messageBodyBytes.length ||
                routingId.isEmpty() || null == messageType);
    }

    /**
     * Set the body content of ZmqMessage to null
     */
    public void clearMsgBytes() {
        this.messageBodyBytes = null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ZmqMessageBOM))
            return false;
        if (obj == this)
            return true;
        final ZmqMessageBOM zmqMessageBOM = (ZmqMessageBOM) obj;
        return (messageType == zmqMessageBOM.getMessageType() &&
                routingId.equals(zmqMessageBOM.getRoutingId()) &&
                Arrays.equals(messageBodyBytes, zmqMessageBOM.getMessageBodyBytes()));
    }
}
