package orwell.proxy.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"port", "attempts", "timeoutPerAttemptMs"})
public class ConfigUdpBroadcast implements IConfigUdpBroadcast {
    private int port;
    private int attempts;
    private int timeoutPerAttemptMs;

    @Override
    public int getAttempts() {
        return attempts;
    }

    @XmlElement
    public void setAttempts(final int attempts) {
        this.attempts = attempts;
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
    public int getTimeoutPerAttemptMs() {
        return timeoutPerAttemptMs;
    }

    @XmlElement
    public void setTimeoutPerAttemptMs(final int timeoutPerAttemptMs) {
        this.timeoutPerAttemptMs = timeoutPerAttemptMs;
    }
}
