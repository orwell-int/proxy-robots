package orwell.proxy.config.elements;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public interface IConfigProxy {

    ConfigServerGame getMaxPriorityConfigServerGame();

    int getReceiveTimeout();

    int getReceiverLinger();

    int getSenderLinger();

    int getOutgoingMsgPeriod();

    ConfigUdpBroadcast getConfigUdpBroadcast();
}
