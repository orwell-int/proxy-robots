package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.Configuration;
import orwell.proxy.config.elements.ConfigRobotsPortsPool;
import orwell.proxy.config.elements.IConfigProxy;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.udp.UdpServerGameFinder;
import orwell.proxy.udp.UdpServerGameFinderFactory;
import orwell.proxy.zmq.ServerGameMessageBroker;

public class ProxyRobotsFactory {
    private final static Logger logback = LoggerFactory.getLogger(ProxyRobotsFactory.class);
    private final ConfigFactory configFactory;
    private final ServerGameMessageBroker serverGameMessageBroker;
    private final UdpServerGameFinder udpServerGameFinder;

    public ProxyRobotsFactory(final Configuration configuration) {
        configFactory = ConfigFactory.createConfigFactory(configuration);

        if (null == configFactory.getConfigProxy()) {
            // We do not have the data to initialize the broker and udp discovery
            assert (false);
            serverGameMessageBroker = null;
            udpServerGameFinder = null;
        } else {

            final IConfigProxy configProxy = configFactory.getConfigProxy();
            serverGameMessageBroker = new ServerGameMessageBroker(
                    configProxy.getReceiveTimeout(),
                    configProxy.getSenderLinger(),
                    configProxy.getReceiverLinger(),
                    configProxy.getOutgoingMsgPeriod()
            );

            udpServerGameFinder = UdpServerGameFinderFactory.fromConfig(
                    configFactory.getConfigProxy().getConfigUdpServerGameFinder()
            );
        }
    }

    public ProxyRobots getProxyRobots() {
        if (null == serverGameMessageBroker) {
            return null;
        }
        final IConfigProxy configProxy = configFactory.getConfigProxy();
        ConfigRobotsPortsPool configRobotsPortsPool = configProxy.getConfigRobotsPortsPool();
        return new ProxyRobots(
                udpServerGameFinder, serverGameMessageBroker,
                configFactory, new RobotsMap(),
                configRobotsPortsPool,
                configProxy.getUdpProxyBroadcastPort()
        );

    }
}
