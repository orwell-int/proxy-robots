package orwell.proxy.mock;

import orwell.proxy.EnumMessageType;
import orwell.proxy.IMessageFramework;
import orwell.proxy.IZmqMessageListener;

/**
 * Created by parapampa on 03/05/15.
 */
public class MockedZmqMessageFramework implements IMessageFramework {

    @Override
    public void setSkipIdenticalMessages(boolean skipIdenticalMessages) {

    }

    @Override
    public boolean connectToServer(String serverIp, int pushPort, int subPort) {
        return true;
    }

    @Override
    public boolean sendZmqMessage(EnumMessageType msgType, String routingID, byte[] msgBytes) {
        return true;
    }

    @Override
    public void addZmqMessageListener(IZmqMessageListener zmqMsgListener) {

    }

    @Override
    public void close() {

    }
}
