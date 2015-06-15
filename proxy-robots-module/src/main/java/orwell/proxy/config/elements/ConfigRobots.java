package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class ConfigRobots implements IConfigRobots {
    private List<ConfigTank> configTanks;
    private List<ConfigScout> configScouts;

    @XmlElement(name = "tank")
    public List<ConfigTank> getConfigTanks() {
        return configTanks;
    }

    public void setConfigTanks(final List<ConfigTank> configTanks) {
        this.configTanks = configTanks;
    }

    @XmlElement(name = "scout")
    public List<ConfigScout> getConfigScouts() {
        return configScouts;
    }

    public void setConfigScouts(final List<ConfigScout> configScouts) {
        this.configScouts = configScouts;
    }

    @Override
    public IConfigRobot getConfigRobot(final String tempRoutingID) throws Exception {
        if (null != configTanks) {
            for (final ConfigTank config : this.configTanks) {
                if (config.getTempRoutingID().contentEquals(tempRoutingID))
                    return config;
            }
        }
        if (null != configScouts) {
            for (final ConfigScout config : this.configScouts) {
                if (config.getTempRoutingID().contentEquals(tempRoutingID))
                    return config;
            }
        }
        throw new Exception("tank " + tempRoutingID
                + " not found in the configuration file");
    }

    @Override
    public ArrayList<IConfigRobot> getConfigRobotsToRegister() {
        final ArrayList<IConfigRobot> configRobotsToRegister = new ArrayList<>();

        if (null != configTanks) {
            for (final ConfigTank configTank : configTanks) {
                if (configTank.shouldRegister()) {
                    configRobotsToRegister.add(configTank);
                }
            }
        }
        if (null != configScouts) {
            for (final ConfigScout configScout : configScouts) {
                if (configScout.shouldRegister()) {
                    configRobotsToRegister.add(configScout);
                }
            }
        }
        return configRobotsToRegister;
    }
}
