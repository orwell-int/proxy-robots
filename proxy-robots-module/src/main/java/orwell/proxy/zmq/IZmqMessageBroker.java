package orwell.proxy.zmq;

/**
 * Created by parapampa on 03/05/15.
 */
public interface IZmqMessageBroker {

    /**
     * Decide whether to handle two identical successive messages or to ignore the second
     *
     * @param skipIdenticalMessages : if true, the second identical message (in a row) will be ignored
     */
    void setSkipIncomingIdenticalMessages(boolean skipIdenticalMessages);

    boolean connectToServer(final String serverIp,
                            final int pushPort,
                            final int subPort);

    boolean sendZmqMessage(final ZmqMessageBOM zmqMessageBOM);

    void addZmqMessageListener(final IZmqMessageListener zmqMsgListener);

    void close();

    boolean isConnectedToServer();
}
