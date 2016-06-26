package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Michaël Ludmann on 23/06/16.
 */
@XmlType(propOrder = {"macAddress", "ipAddress"})
public class ConfigNetworkInterface implements IConfigNetworkInterface {

    private String macAddress;
    private String ipAddress;
    private String networkAddress;

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    @Override
    @XmlElement(name = "mac")
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    @XmlElement(name = "ip")
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    @XmlAttribute(name = "addr")
    public String getNetworkAddress() {
        return networkAddress;
    }

    @Override
    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }
}
