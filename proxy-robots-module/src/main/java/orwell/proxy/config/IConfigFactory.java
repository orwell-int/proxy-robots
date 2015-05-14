package orwell.proxy.config;

/**
 * Created by miludmann on 5/11/15.
 */
public interface IConfigFactory {
    public IConfigProxy getConfigProxy();

    public IConfigRobots getConfigRobots();

    public IConfigServerGame getConfigServerGame();
}
