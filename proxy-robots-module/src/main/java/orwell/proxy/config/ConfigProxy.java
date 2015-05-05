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

    /**
     * @return the server game configuration of the highest priority
     *         It is the first one found by default.
     */
    @Override
    public ConfigServerGame getConfigServerGame() {
        ConfigServerGame priorityConfig = null;
        for (ConfigServerGame config : this.configServerGames) {
            if (null == priorityConfig || config.getPriority() > priorityConfig.getPriority()) {
                priorityConfig = config;
            }
        }
        return priorityConfig;
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
