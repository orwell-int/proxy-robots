package orwell.proxy.config;

/**
 * Created by Michaël Ludmann on 5/28/15.
 */
public interface IConfigUdpBroadcast {
    int getAttempts();

    int getPort();

    int getTimeoutPerAttemptMs();
}
