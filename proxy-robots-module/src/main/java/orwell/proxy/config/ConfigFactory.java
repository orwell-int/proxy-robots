package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public class ConfigFactory implements IConfigFactory {
    private final static Logger logback = LoggerFactory.getLogger(ConfigFactory.class);

    private IConfigProxy configProxy;
    private IConfigRobots configRobots;
    private IConfigServerGame configServerGame;

    private ConfigFactory(final Configuration configuration) {
        logback.debug("Constructor -- IN");
        configuration.populate();

        if (!configuration.isPopulated()) {
            logback.error("Configuration loading error");
        } else {
            final ConfigModel configModel = configuration.getConfigModel();
            configProxy = configModel.getConfigProxy();
            configRobots = configuration.getConfigModel().getConfigRobots();
            configServerGame = configProxy.getMaxPriorityConfigServerGame();
        }
        logback.debug("Constructor -- OUT");
    }

    public static ConfigFactory createConfigFactory(final Configuration configuration) {
        return new ConfigFactory(configuration);
    }

    @Override
    public IConfigProxy getConfigProxy() {
        return configProxy;
    }

    @Override
    public IConfigRobots getConfigRobots() {
        return configRobots;
    }

    @Override
    public IConfigServerGame getMaxPriorityConfigServerGame() {
        return configServerGame;
    }
}
