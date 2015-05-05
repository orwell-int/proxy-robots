package orwell.proxy.config;

/**
 * Created by parapampa on 03/05/15.
 */
public interface IConfigProxy {

    ConfigServerGame getConfigServerGame(String name) throws Exception;

    int getReceiverLinger();

    int getSenderLinger();
}
