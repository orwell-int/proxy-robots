package orwell.proxy.zmq;

public interface IServerGameMessageBroker {

    /**
     * Decide whether to handle two identical successive messages or to ignore the second
     *
     * @param skipIdenticalMessages : if true, the second identical message (in a row) will be ignored
     */
    void setSkipIncomingIdenticalMessages(boolean skipIdenticalMessages);

    boolean connectToServer(final String pushAddress, final String subscribeAddress);

    boolean sendZmqMessage(final ZmqMessageBOM zmqMessageBOM);

    void addZmqMessageListener(final IZmqMessageListener zmqMsgListener);

    void close();

    boolean isConnectedToServer();

    int getOutgoingMessagePeriod();
}
