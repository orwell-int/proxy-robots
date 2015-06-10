package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"ip", "port", "resourcePath"})
public class ConfigCamera implements IConfigCamera {
    private String ip;
    private int port;
    private String resourcePath;

    @Override
    public String getIp() {
        return ip;
    }

    @XmlElement
    public void setIp(final String ip) {
        this.ip = ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @XmlElement
    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    @XmlElement
    public void setResourcePath(final String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
