package orwell.proxy;

import orwell.proxy.config.ConfigFactoryParameters;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.zmq.FrequencyFilter;
import orwell.proxy.zmq.IFilter;
import orwell.proxy.zmq.ZmqMessageBroker;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public class ProxyRobotsFactory {
    private final ConfigFactory configFactory;
    private final ZmqMessageBroker zmqMessageFramework;

    public ProxyRobotsFactory(final ConfigFactoryParameters configPathType) {
        configFactory = new ConfigFactory(configPathType);

        if (null == configFactory.getConfigProxy()) {
            zmqMessageFramework = null;
        } else {
            zmqMessageFramework = new ZmqMessageBroker(configFactory.getConfigProxy().getSenderLinger(),
                    configFactory.getConfigProxy().getReceiverLinger(),
                    null);
        }
    }

    public ProxyRobots getProxyRobots() {
        if (null == zmqMessageFramework) {
            return null;
        }
        return new ProxyRobots(zmqMessageFramework, configFactory, new RobotsMap());

    }

    // TODO Frequency filter should be revised ; we do not want to filter
    // necessary messages for the server
    private ArrayList<IFilter> getFilterList() {
        final ArrayList<IFilter> filterList = new ArrayList<>();
        if(null != configFactory.getConfigProxy())
            filterList.add(new FrequencyFilter(configFactory.getConfigProxy().getOutgoingMsgPeriod()));

        return filterList;
    }
}
