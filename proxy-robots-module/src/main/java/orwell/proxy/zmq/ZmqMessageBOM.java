package orwell.proxy.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumMessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

/**
 * Created by Michaël Ludmann on 5/6/15.
 */
public class ZmqMessageBOM implements Comparable<ZmqMessageBOM> {
    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageBOM.class);

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
     *                        routingID typeString message
     */
    public static ZmqMessageBOM parseFrom(final byte[] raw_zmq_message) throws ParseException {
        final String zmqMessageString = new String(raw_zmq_message);
        final String[] zmqMessageStringArray = zmqMessageString.split(" ", 3);

        if (3 != zmqMessageStringArray.length) {
            logback.warn("ZmqMessage failed to split incoming message, missing items: " +
                    zmqMessageString);
            throw new ParseException("Message does not contain all three mandatory items", 3);
        }

        final String routingId = zmqMessageStringArray[0];
        final String typeString = zmqMessageStringArray[1];
        final byte[] messageBodyBytes = zmqMessageStringArray[2].getBytes();
        final EnumMessageType type;
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

        logback.info("Message parsed: [RoutingID] " + routingId + " [TYPE]: " + type);
        return new ZmqMessageBOM(routingId, type, messageBodyBytes);
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
    public int compareTo(final ZmqMessageBOM zmqMessageBOM) {
        if (null == zmqMessageBOM || messageType != zmqMessageBOM.getMessageType() ||
                0 != routingId.compareTo(zmqMessageBOM.getRoutingId()) ||
                !Arrays.equals(messageBodyBytes, zmqMessageBOM.getMessageBodyBytes()))
            return 1;
        else return 0;
    }
}
