package orwell.proxy.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"port", "attempts", "timeoutPerAttemptMs"})
public class ConfigUdpBroadcast {
    private int port;
    private int attempts;
    private int timeoutPerAttemptMs;

    public int getAttempts() {
        return attempts;
    }

    @XmlElement
    public void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    public int getPort() {
        return port;
    }

    @XmlElement
    public void setPort(final int port) {
        this.port = port;
    }

    public int getTimeoutPerAttemptMs() {
        return timeoutPerAttemptMs;
    }

    @XmlElement
    public void setTimeoutPerAttemptMs(final int timeoutPerAttemptMs) {
        this.timeoutPerAttemptMs = timeoutPerAttemptMs;
    }
}
