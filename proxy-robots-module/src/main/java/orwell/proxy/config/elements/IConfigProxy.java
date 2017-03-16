package orwell.proxy.config.elements;

public interface IConfigProxy {

    ConfigServerGame getMaxPriorityConfigServerGame();

    int getUdpProxyBroadcastPort();

    int getReceiveTimeout();

    int getReceiverLinger();

    int getSenderLinger();

    int getOutgoingMsgPeriod();

    ConfigUdpServerGameFinder getConfigUdpServerGameFinder();

    ConfigRobotsPortsPool getConfigRobotsPortsPool();
}
