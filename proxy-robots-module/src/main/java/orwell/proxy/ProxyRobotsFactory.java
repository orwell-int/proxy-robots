package orwell.proxy;

import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.ConfigFactory;

/**
 * Created by parapampa on 03/05/15.
 */
public class ProxyRobotsFactory {
    private final ConfigFactory configFactory;
    private final ZmqMessageFramework zmqMessageFramework;

    public ProxyRobotsFactory(ConfigCli configPathType, String serverName) {
        configFactory = new ConfigFactory(configPathType);
        zmqMessageFramework = new ZmqMessageFramework(configFactory.getConfigProxy().getSenderLinger(),
                configFactory.getConfigProxy().getReceiverLinger());
    }

    public ProxyRobots getProxyRobots() {
        return new ProxyRobots(zmqMessageFramework,
                configFactory.getConfigServerGame(),
                configFactory.getConfigRobots(),
                new RobotsMap());
    }
}
