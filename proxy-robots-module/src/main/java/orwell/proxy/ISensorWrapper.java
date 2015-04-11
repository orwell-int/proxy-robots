package orwell.proxy;

import com.google.protobuf.MessageLiteOrBuilder;

/**
 * Created by parapampa on 11/04/15.
 */
public interface ISensorWrapper {

    public MessageLiteOrBuilder getBuilder();
    public String getPreviousValue();
    public void setPreviousValue(String previousValue);
}
