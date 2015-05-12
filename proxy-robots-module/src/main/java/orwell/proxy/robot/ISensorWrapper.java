package orwell.proxy.robot;

import com.google.protobuf.MessageLiteOrBuilder;

/**
 * Created by Michaël Ludmann on 11/04/15.
 */
public interface ISensorWrapper {

    public MessageLiteOrBuilder getBuilder();

    public String getPreviousValue();

    public void setPreviousValue(final String previousValue);
}
