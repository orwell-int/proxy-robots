package orwell.proxy;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ConfigRobots {
	private List<ConfigTank> configTanks;
	
	@XmlElement(name="tank")
    public List<ConfigTank> getConfigTanks() {
        return configTanks;
    }
	
    public void setConfigTanks(List<ConfigTank> configTanks) {
        this.configTanks = configTanks;
    }
    
    public ConfigTank getConfigTank(String networkID) throws Exception {
    	for (ConfigTank config: this.configTanks) {
			if(config.getNetworkID().contentEquals(networkID))
				return config;
		}

    	throw new Exception("tank " + networkID + " not found in the configuration file");
    }
}
