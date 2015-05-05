package orwell.proxy.config;

/**
 * Created by parapampa on 03/05/15.
 */
public interface IConfigProxy {

    ConfigServerGame getConfigServerGame() throws Exception;

    int getReceiverLinger();

    int getSenderLinger();
}
