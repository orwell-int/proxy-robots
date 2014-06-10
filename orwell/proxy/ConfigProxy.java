package orwell.proxy;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ConfigProxy {
	
	private List<ConfigServerGame> configServerGames;
	
	@XmlElement(name="server-game")
    public List<ConfigServerGame> getConfigServerGames() {
        return configServerGames;
    }
	
    public void setConfigServerGames(List<ConfigServerGame> configServerGames) {
        this.configServerGames = configServerGames;
    }
    
    public ConfigServerGame getConfigServerGame(String name) throws Exception {
    	for (ConfigServerGame config: this.configServerGames) {
			if(config.getName().contentEquals(name))
				return config;
		}

    	throw new Exception("server-game " + name + " not found in the configuration file");
    }
}
