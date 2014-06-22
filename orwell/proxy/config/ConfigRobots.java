package orwell.proxy.config;

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

	public ConfigTank getConfigTank(String routingID) throws Exception {
		for (ConfigTank config : this.configTanks) {
			if (config.getRoutingID().contentEquals(routingID))
				return config;
		}

		throw new Exception("tank " + routingID
				+ " not found in the configuration file");
	}
}
