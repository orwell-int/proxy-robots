package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Michaël Ludmann on 27/06/16.
 */
public class ConfigMessaging {
    private int pushPort;
    private int pullPort;

    public int getPushPort() {
        return pushPort;
    }

    @XmlElement
    public void setPushPort(int pushPort) {
        this.pushPort = pushPort;
    }

    public int getPullPort() {
        return pullPort;
    }

    @XmlElement
    public void setPullPort(int pullPort) {
        this.pullPort = pullPort;
    }
}
