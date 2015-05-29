package orwell.proxy;

import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.ConfigFactoryParameters;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.zmq.FrequencyFilter;
import orwell.proxy.zmq.IFilter;
import orwell.proxy.zmq.ZmqMessageBroker;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
class ProxyRobotsFactory {
    private final ConfigFactory configFactory;
    private final ZmqMessageBroker zmqMessageBroker;
    private final UdpBeaconFinder udpBeaconFinder;

    public ProxyRobotsFactory(final ConfigFactoryParameters configPathType) {
        configFactory = ConfigFactory.createConfigFactory(configPathType);

        if (null == configFactory.getConfigProxy()) {
            // We do not have the data to initialize the broker and udp discovery
            zmqMessageBroker = null;
            udpBeaconFinder = null;
        } else {
            zmqMessageBroker = new ZmqMessageBroker(configFactory.getConfigProxy().getReceiveTimeout(),
                    configFactory.getConfigProxy().getSenderLinger(),
                    configFactory.getConfigProxy().getReceiverLinger(),
                    null);
            udpBeaconFinder = UdpBeaconFinderFactory.fromConfig(configFactory.getConfigProxy().getConfigUdpBroadcast());
        }
    }

    public ProxyRobots getProxyRobots() {
        if (null == zmqMessageBroker) {
            return null;
        }
        return new ProxyRobots(udpBeaconFinder, zmqMessageBroker, configFactory, new RobotsMap());

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
