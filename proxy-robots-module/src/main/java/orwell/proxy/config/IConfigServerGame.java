package orwell.proxy.config;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public interface IConfigServerGame {
    String getName();

    String getIp();

    int getPushPort();

    int getSubPort();
}
