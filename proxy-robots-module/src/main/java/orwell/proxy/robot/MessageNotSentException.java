package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 03/07/16.
 */
public class MessageNotSentException extends Exception {
    final private String hostname;

    public MessageNotSentException(final String hostname) {
        this.hostname = hostname;
    }

    public String getMessage() {
        return "Unable to send message to robot [" + hostname + "]";
    }
}
