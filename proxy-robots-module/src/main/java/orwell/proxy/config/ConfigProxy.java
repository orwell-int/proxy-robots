package orwell.proxy.config;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class ConfigProxy implements IConfigProxy {

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

    @Override
    public ConfigServerGame getConfigServerGame(String name) throws Exception {
        for (ConfigServerGame config : this.configServerGames) {
            if (config.getName().contentEquals(name))
                return config;
        }

        throw new Exception("server-game " + name
                + " not found in the configuration file");
    }

    @Override
    public int getSenderLinger() {
        return senderLinger;
    }

    @XmlElement(name = "senderLinger")
    public void setSenderLinger(int senderLinger) {
        this.senderLinger = senderLinger;
    }

    @Override
    public int getReceiverLinger() {
        return receiverLinger;
    }

    @XmlElement(name = "receiverLinger")
    public void setReceiverLinger(int receiverLinger) {
        this.receiverLinger = receiverLinger;
    }
}
