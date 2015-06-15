package orwell.proxy.config;

import orwell.proxy.config.elements.IConfigProxy;
import orwell.proxy.config.elements.IConfigRobots;
import orwell.proxy.config.elements.IConfigServerGame;

/**
 * Created by Michaël Ludmann on 5/11/15.
 */
public interface IConfigFactory {
    public IConfigProxy getConfigProxy();

    public IConfigRobots getConfigRobots();

    public IConfigServerGame getMaxPriorityConfigServerGame();
}
