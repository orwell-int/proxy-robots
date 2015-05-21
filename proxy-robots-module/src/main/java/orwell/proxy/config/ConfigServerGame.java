package orwell.proxy.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"ip", "pushPort", "subPort"})
public class ConfigServerGame implements IConfigServerGame {

    private String name;
    private String ip;
    private int pushPort;
    private int subPort;
    private int priority;

    @Override
    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String name) {
        this.name = name;
    }

    /*
     * @returns the priority of the server in the conf
     */
    public int getPriority() {
        return priority;
    }

    @XmlAttribute
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @XmlElement
    public void setIp(final String ip) {
        this.ip = ip;
    }

    @Override
    public int getPushPort() {
        return pushPort;
    }

    @XmlElement
    public void setPushPort(final int pushPort) {
        this.pushPort = pushPort;
    }

    @Override
    public int getSubPort() {
        return subPort;
    }

    @XmlElement
    public void setSubPort(final int subPort) {
        this.subPort = subPort;
    }

}
