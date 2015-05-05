package orwell.proxy;

import orwell.proxy.config.ConfigFactory;

/**
 * Created by parapampa on 03/05/15.
 */
public class ProxyRobotsFactory {
    private ConfigFactory configFactory;
    private ZmqMessageFramework zmqMessageFramework;

    public ProxyRobotsFactory(String confFileAddress, String serverName) {
        configFactory = new ConfigFactory(confFileAddress, serverName);
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
