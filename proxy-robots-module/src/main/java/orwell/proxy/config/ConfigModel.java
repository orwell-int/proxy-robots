package orwell.proxy.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "setup")
public class ConfigModel {

    private ConfigProxy configProxy;
    private ConfigRobots configRobots;

    @XmlElement(name = "proxy")
    public ConfigProxy getConfigProxy() {
        return configProxy;
    }

    public void setConfigProxy(final ConfigProxy configProxy) {
        this.configProxy = configProxy;
    }

    @XmlElement(name = "robots")
    public ConfigRobots getConfigRobots() {
        return configRobots;
    }

    public void setConfigRobots(final ConfigRobots configRobots) {
        this.configRobots = configRobots;
    }
}
