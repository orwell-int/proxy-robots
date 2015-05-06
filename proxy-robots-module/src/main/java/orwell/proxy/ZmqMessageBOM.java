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
    private final String routingID;
    private byte[] msgBytes;

    public ZmqMessageBOM(final EnumMessageType msgType,
                         final String routingID,
                         final byte[] msgBytes) {
        this.msgType = msgType;
        this.routingID = routingID;
        this.msgBytes = msgBytes;
    }

    public EnumMessageType getMsgType() {
        return msgType;
    }

    public String getRoutingID() {
        return routingID;
    }

    /*
     * returns body of the message
     */
    public byte[] getMsgBodyBytes() {
        return msgBytes;
    }

    public byte[] getZmqMessageBytes() {
        StringBuilder zmqMessageHeaderBuilder = new StringBuilder();
        zmqMessageHeaderBuilder.append(routingID).append(" ");

//        String zmqMessageHeader = routingID + " ";
        switch (msgType) {
            case REGISTER:
//                zmqMessageHeader += "Register ";
                zmqMessageHeaderBuilder.append("Register ");
                break;
            case SERVER_ROBOT_STATE:
//                zmqMessageHeader += "ServerRobotState ";
                zmqMessageHeaderBuilder.append("ServerRobotState ");
                break;
            default:

        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
//            outputStream.write(zmqMessageHeader.getBytes());
            outputStream.write(zmqMessageHeaderBuilder.toString().getBytes());
            outputStream.write(msgBytes);
        } catch (IOException e) {
            logback.error("Error while building zmqMessage " + e.getMessage());
            return null;
        }
        return outputStream.toByteArray();
    }

    public boolean isEmpty() {
        return (null == msgBytes || routingID.isEmpty() || null == msgType);
    }

    /*
     * Set core content of ZmqMessage to null
     */
    public void clearMsgBytes() {
        this.msgBytes = null;
    }
}
