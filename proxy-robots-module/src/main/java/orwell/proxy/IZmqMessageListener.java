package orwell.proxy;

/**
 * Created by parapampa on 08/03/15.
 */
public interface IZmqMessageListener {
    void receivedNewZmq(ZmqMessageWrapper msg);
}
