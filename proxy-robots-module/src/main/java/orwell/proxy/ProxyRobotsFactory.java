package orwell.proxy;

import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.ConfigFactory;

import java.util.ArrayList;

/**
 * Created by parapampa on 03/05/15.
 */
public class ProxyRobotsFactory {
    private final ConfigFactory configFactory;
    private final ZmqMessageFramework zmqMessageFramework;

    public ProxyRobotsFactory(final ConfigCli configPathType) {
        configFactory = new ConfigFactory(configPathType);

        zmqMessageFramework = new ZmqMessageFramework(configFactory.getConfigProxy().getSenderLinger(),
                configFactory.getConfigProxy().getReceiverLinger(),
                getFilterList());
    }

    public ProxyRobots getProxyRobots() {
        return new ProxyRobots(zmqMessageFramework,
                configFactory.getConfigServerGame(),
                configFactory.getConfigRobots(),
                new RobotsMap());
    }

    private ArrayList<IFilter> getFilterList(){
        final ArrayList<IFilter> filterList = new ArrayList<>();
        filterList.add(new FrequencyFilter(configFactory.getConfigProxy().getOutgoingMsgFrequency()));

        return filterList;
    }
}
