package orwell.proxy.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ConfigProxy {

	private List<ConfigServerGame> configServerGames;
	private int senderLinger;
	private int receiverLinger;

	@XmlElement(name = "server-game")
	public List<ConfigServerGame> getConfigServerGames() {
		return configServerGames;
	}

	public void setConfigServerGames(List<ConfigServerGame> configServerGames) {
		this.configServerGames = configServerGames;
	}

	public ConfigServerGame getConfigServerGame(String name) throws Exception {
		for (ConfigServerGame config : this.configServerGames) {
			if (config.getName().contentEquals(name))
				return config;
		}

		throw new Exception("server-game " + name
				+ " not found in the configuration file");
	}

	public int getSenderLinger() {
		return senderLinger;
	}

	@XmlElement(name = "senderLinger")
	public void setSenderLinger(int senderLinger) {
		this.senderLinger = senderLinger;
	}

	public int getReceiverLinger() {
		return receiverLinger;
	}

	@XmlElement(name = "receiverLinger")
	public void setReceiverLinger(int receiverLinger) {
		this.receiverLinger = receiverLinger;
	}
}
