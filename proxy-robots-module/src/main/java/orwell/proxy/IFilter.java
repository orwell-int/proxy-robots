package orwell.proxy;

/**
 * Created by miludmann on 5/6/15.
 */
public interface IFilter {

    ZmqMessageBOM getFilteredMessage(ZmqMessageBOM inputMessage);
}
