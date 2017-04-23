package orwell.proxy.config.elements;

public interface IConfigUdpServerGameFinder {
    int getAttempts();

    int getPort();

    int getTimeoutPerAttemptMs();
}
