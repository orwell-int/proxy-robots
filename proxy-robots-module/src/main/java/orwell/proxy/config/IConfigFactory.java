package orwell.proxy.config;

import orwell.proxy.config.elements.IConfigProxy;
import orwell.proxy.config.elements.IConfigRobots;
import orwell.proxy.config.elements.IConfigServerGame;

public interface IConfigFactory {
    IConfigProxy getConfigProxy();

    IConfigRobots getConfigRobots();

    IConfigServerGame getMaxPriorityConfigServerGame();
}
