package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.Configuration;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.udp.UdpBeaconFinder;
import orwell.proxy.udp.UdpBeaconFinderFactory;
import orwell.proxy.zmq.FrequencyFilter;
import orwell.proxy.zmq.GameServerMessageBroker;
import orwell.proxy.zmq.IFilter;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
class ProxyRobotsFactory {
    private final static Logger logback = LoggerFactory.getLogger(ProxyRobotsFactory.class);
    private final ConfigFactory configFactory;
    private final GameServerMessageBroker gameServerMessageBroker;
    private final UdpBeaconFinder udpBeaconFinder;

    public ProxyRobotsFactory(final Configuration configuration) {
        logback.debug("Constructor -- IN");
        configFactory = ConfigFactory.createConfigFactory(configuration);

        if (null == configFactory.getConfigProxy()) {
            // We do not have the data to initialize the broker and udp discovery
            gameServerMessageBroker = null;
            udpBeaconFinder = null;
        } else {

            gameServerMessageBroker = new GameServerMessageBroker(
                    configFactory.getConfigProxy().getReceiveTimeout(),
                    configFactory.getConfigProxy().getSenderLinger(),
                    configFactory.getConfigProxy().getReceiverLinger()
            );

            udpBeaconFinder = UdpBeaconFinderFactory.fromConfig(
                    configFactory.getConfigProxy().getConfigUdpBroadcast()
            );
        }
        logback.debug("Constructor -- OUT");
    }

    public ProxyRobots getProxyRobots() {
        if (null == gameServerMessageBroker) {
            return null;
        }
        return new ProxyRobots(
                udpBeaconFinder, gameServerMessageBroker,
                configFactory, new RobotsMap()
        );

    }

    // TODO Frequency filter should be revised ; we do not want to filter
    // necessary messages for the server
    private ArrayList<IFilter> getFilterList() {
        final ArrayList<IFilter> filterList = new ArrayList<>();
        if (null != configFactory.getConfigProxy())
            filterList.add(new FrequencyFilter(configFactory.getConfigProxy().getOutgoingMsgPeriod()));

        return filterList;
    }
}
