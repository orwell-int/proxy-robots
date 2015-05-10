package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by parapampa on 03/05/15.
 */
public class ConfigFactory {
    final static Logger logback = LoggerFactory.getLogger(ConfigFactory.class);

    private final IConfigProxy configProxy;
    private final IConfigRobots configRobots;
    private final IConfigServerGame configServerGame;

    public ConfigFactory(final ConfigFactoryParameters configFactoryParameters) {
        logback.debug("IN");
        final Configuration configuration = new Configuration(configFactoryParameters);

        if (false == configuration.isPopulated) {
            logback.error("Configuration loading error");
            configProxy = null;
            configRobots = null;
            configServerGame = null;
        } else {
            final ConfigModel configModel = configuration.getConfigModel();
            configProxy = configModel.getConfigProxy();
            configRobots = configuration.getConfigModel().getConfigRobots();
            configServerGame = configProxy.getConfigServerGame();
        }
        logback.debug("OUT");
    }

    public IConfigProxy getConfigProxy() {
        return configProxy;
    }

    public IConfigRobots getConfigRobots() {
        return configRobots;
    }

    public IConfigServerGame getConfigServerGame() {
        return configServerGame;
    }
}
