package orwell.proxy.zmq;

/**
 * Created by Michaël Ludmann on 5/6/15.
 */
public interface IFilter {

    ZmqMessageBOM getFilteredMessage(ZmqMessageBOM inputMessage);
}
