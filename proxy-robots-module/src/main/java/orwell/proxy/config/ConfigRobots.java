package orwell.proxy.config;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class ConfigRobots implements IConfigRobots {
    private List<ConfigTank> configTanks;

    @XmlElement(name = "tank")
    public List<ConfigTank> getConfigTanks() {
        return configTanks;
    }

    public void setConfigTanks(final List<ConfigTank> configTanks) {
        this.configTanks = configTanks;
    }

    @Override
    public ConfigTank getConfigTank(final String tempRoutingID) throws Exception {
        for (final ConfigTank config : this.configTanks) {
            if (config.getTempRoutingID().contentEquals(tempRoutingID))
                return config;
        }

        throw new Exception("tank " + tempRoutingID
                + " not found in the configuration file");
    }

    @Override
    public ArrayList<ConfigTank> getConfigRobotsToRegister() {
        final ArrayList<ConfigTank> configRobotsToRegister = new ArrayList<>();
        for (final ConfigTank configTank : this.configTanks) {
            if (configTank.shouldRegister()) {
                configRobotsToRegister.add(configTank);
            }
        }
        return configRobotsToRegister;
    }
}
