package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlElement;

public class ConfigRobotsPortsPool {
    private int beginPort;
    private int portsCount;

    public int getBeginPort() {
        return beginPort;
    }

    @XmlElement
    public void setBeginPort(int beginPort) {
        this.beginPort = beginPort;
    }

    public int getPortsCount() {
        return portsCount;
    }

    @XmlElement
    public void setPortsCount(int portsCount) {
        this.portsCount = portsCount;
    }
}
