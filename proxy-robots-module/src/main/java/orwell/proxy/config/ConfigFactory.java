package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.elements.IConfigProxy;
import orwell.proxy.config.elements.IConfigRobots;
import orwell.proxy.config.elements.IConfigServerGame;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public class ConfigFactory implements IConfigFactory {
    private final static Logger logback = LoggerFactory.getLogger(ConfigFactory.class);

    private IConfigProxy configProxy;
    private IConfigRobots configRobots;
    private IConfigServerGame configServerGame;

    private ConfigFactory(final Configuration configuration) {
        configuration.populate();

        if (!configuration.isPopulated()) {
            logback.error("Configuration loading error");
        } else {
            final ConfigModel configModel = configuration.getConfigModel();
            configProxy = configModel.getConfigProxy();
            configRobots = configuration.getConfigModel().getConfigRobots();
            configServerGame = configProxy.getMaxPriorityConfigServerGame();
        }
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
