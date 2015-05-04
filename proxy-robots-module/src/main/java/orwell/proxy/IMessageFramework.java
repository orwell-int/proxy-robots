package orwell.proxy;

/**
 * Created by parapampa on 03/05/15.
 */
public interface IMessageFramework {

    /**
     * Decide whether to handle two identical successive messages or to ignore the second
     * @param skipIdenticalMessages : if true, the second identical message (in a row) will be ignored
     */
    void setSkipIdenticalMessages(boolean skipIdenticalMessages);

    boolean connectToServer(String serverIp,
                            int pushPort,
                            int subPort);

    boolean sendZmqMessage(EnumMessageType msgType,
                           String routingID,
                           byte[] msgBytes);

    void addZmqMessageListener(IZmqMessageListener zmqMsgListener);

    void close();
}
