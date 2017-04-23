package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class ConfigProxy implements IConfigProxy {

    private List<ConfigServerGame> configServerGames;
    private int receiveTimeout;
    private int senderLinger;
    private int receiverLinger;
    private int outgoingMsgPeriod;
    private ConfigUdpServerGameFinder configUdpServerGameFinder;
    private ConfigRobotsPortsPool configRobotsPortsPool;
    private int udpProxyBroadcastPort;

    @XmlElement(name = "server-game")
    public List<ConfigServerGame> getConfigServerGames() {
        return configServerGames;
    }

    public void setConfigServerGames(final List<ConfigServerGame> configServerGames) {
        this.configServerGames = configServerGames;
    }

    /**
     * @return the server game configuration of the highest priority
     * If no priority defined, it is the first one found by default.
     */
    @Override
    public ConfigServerGame getMaxPriorityConfigServerGame() {
        ConfigServerGame maxPriorityConfig = null;
        for (final ConfigServerGame config : this.configServerGames) {
            if (null == maxPriorityConfig || config.getPriority() > maxPriorityConfig.getPriority()) {
                maxPriorityConfig = config;
            }
        }
        return maxPriorityConfig;
    }

    @Override
    public int getUdpProxyBroadcastPort() {
        return udpProxyBroadcastPort;
    }

    @XmlElement
    public void setUdpProxyBroadcastPort(final int udpProxyBroadcastPort) {
        this.udpProxyBroadcastPort = udpProxyBroadcastPort;
    }

    @Override
    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    @XmlElement(name = "receiveTimeout")
    public void setReceiveTimeout(final int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }


    @Override
    public int getSenderLinger() {
        return senderLinger;
    }

    @XmlElement(name = "senderLinger")
    public void setSenderLinger(final int senderLinger) {
        this.senderLinger = senderLinger;
    }

    @Override
    public int getReceiverLinger() {
        return receiverLinger;
    }

    @XmlElement(name = "receiverLinger")
    public void setReceiverLinger(final int receiverLinger) {
        this.receiverLinger = receiverLinger;
    }

    @Override
    public int getOutgoingMsgPeriod() {
        return outgoingMsgPeriod;
    }

    @XmlElement(name = "outgoingMsgPeriod")
    public void setOutgoingMsgPeriod(final int outgoingMsgPeriod) {
        this.outgoingMsgPeriod = outgoingMsgPeriod;
    }

    public ConfigUdpServerGameFinder getConfigUdpServerGameFinder() {
        return configUdpServerGameFinder;
    }

    @XmlElement(name = "udpServerGameFinder")
    public void setConfigUdpServerGameFinder(final ConfigUdpServerGameFinder configUdpServerGameFinder) {
        this.configUdpServerGameFinder = configUdpServerGameFinder;
    }

    @Override
    public ConfigRobotsPortsPool getConfigRobotsPortsPool() {
        return configRobotsPortsPool;
    }

    @XmlElement(name = "robotsPortsPool")
    public void setConfigRobotsPortsPool(ConfigRobotsPortsPool configRobotsPortsPool) {
        this.configRobotsPortsPool = configRobotsPortsPool;
    }
}
