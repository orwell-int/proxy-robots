package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by miludmann on 5/6/15.
 */
public class ZmqMessageBOM {
    final static Logger logback = LoggerFactory.getLogger(ZmqMessageBOM.class);

    private final EnumMessageType msgType;
    private final String routingId;
    private byte[] msgBytes;

    public ZmqMessageBOM(final EnumMessageType msgType,
                         final String routingId,
                         final byte[] msgBytes) {
        this.msgType = msgType;
        this.routingId = routingId;
        this.msgBytes = msgBytes;
    }

    public EnumMessageType getMsgType() {
        return msgType;
    }

    public String getRoutingId() {
        return routingId;
    }

    /**
     * @return the body of the message
     */
    public byte[] getMsgBodyBytes() {
        return msgBytes;
    }

    /**
     * @return the whole Zmq message that can be sent as is
     */
    public byte[] getZmqMessageBytes() {
        final StringBuilder zmqMessageHeaderBuilder = new StringBuilder();
        zmqMessageHeaderBuilder.append(routingId).append(" ");

        switch (msgType) {
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
            outputStream.write(msgBytes);
        } catch (IOException e) {
            logback.error("Error while building zmqMessage " + e.getMessage());
            return null;
        }
        return outputStream.toByteArray();
    }

    public boolean isEmpty() {
        return (null == msgBytes || 0 == msgBytes.length || routingId.isEmpty() || null == msgType);
    }

    /**
     * Set core content of ZmqMessage to null
     */
    public void clearMsgBytes() {
        this.msgBytes = null;
    }

}
