package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

/**
 * Created by parapampa on 03/05/15.
 */
public class ConfigFactory {
    final static Logger logback = LoggerFactory.getLogger(ConfigFactory.class);

    private IConfigProxy configProxy;
    private IConfigRobots configRobots;
    private IConfigServerGame configServerGame;

    public ConfigFactory(ConfigCli configCli, String serverGame) {
        Configuration configuration = new Configuration(configCli);
        try {
            // TODO Include populate into default constructor
            configuration.populate();
        } catch (JAXBException e1) {
            logback.error(e1.toString());
        }
        ConfigModel configModel = configuration.getConfigModel();
        configProxy = configModel.getConfigProxy();
        configRobots = configuration.getConfigModel().getConfigRobots();
        try {
            configServerGame = configProxy.getConfigServerGame(serverGame);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
