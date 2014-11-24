package orwell.proxy.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ConfigRobots {
	private List<ConfigTank> configTanks;

	@XmlElement(name = "tank")
	public List<ConfigTank> getConfigTanks() {
		return configTanks;
	}

	public void setConfigTanks(List<ConfigTank> configTanks) {
		this.configTanks = configTanks;
	}

	public ConfigTank getConfigTank(String tempRoutingID) throws Exception {
		for (ConfigTank config : this.configTanks) {
			if (config.getTempRoutingID().contentEquals(tempRoutingID))
				return config;
		}

		throw new Exception("tank " + tempRoutingID
				+ " not found in the configuration file");
	}
	
	public ArrayList<ConfigTank> getConfigRobotsToRegister() {
		ArrayList<ConfigTank> configRobotsToRegister = new ArrayList<ConfigTank>();
		for (ConfigTank configTank : this.configTanks) {
			if(1 == configTank.getToRegister()) {
				configRobotsToRegister.add(configTank);
			}
		}
		return configRobotsToRegister;
	}
}