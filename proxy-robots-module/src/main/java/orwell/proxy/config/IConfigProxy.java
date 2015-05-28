package orwell.proxy.config;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public interface IConfigProxy {

    ConfigServerGame getMaxPriorityConfigServerGame();

    int getReceiverLinger();

    int getSenderLinger();

    int getOutgoingMsgPeriod();

    ConfigUdpBroadcast getConfigUdpBroadcast();
}
