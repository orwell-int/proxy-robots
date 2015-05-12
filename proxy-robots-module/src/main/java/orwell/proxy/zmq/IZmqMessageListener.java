package orwell.proxy.zmq;

/**
 * Created by Michaël Ludmann on 08/03/15.
 */
public interface IZmqMessageListener {
    void receivedNewZmq(ZmqMessageBOM zmqMessageBOM);
}
