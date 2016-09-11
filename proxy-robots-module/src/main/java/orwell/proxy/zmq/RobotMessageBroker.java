package orwell.proxy.zmq;

import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageBuilder;
import lejos.mf.common.UnitMessageType;
import lejos.mf.common.exception.UnitMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 27/06/16.
 */
public class RobotMessageBroker {
    private final static Logger logback = LoggerFactory.getLogger(RobotMessageBroker.class);
    private static final String BINDING_ADDRESS = "tcp://0.0.0.0";
    // This sleep is very important - we tested this value to be enough. Without it, we might lose the first message sent by the server.
    private static final long THREAD_SLEEP_POST_CONNECT_MS = 1000;
    private final ZMQ.Context context;
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    private final int pushPort;
    private final int pullPort;
    private final RobotZmqReader reader;
    private ArrayList<MessageListenerInterface> messageListeners;
    private boolean isConnected = false;

    public RobotMessageBroker(int pushPort, int pullPort) {
        messageListeners = new ArrayList<>();
        this.pushPort = pushPort;
        this.pullPort = pullPort;

        context = ZMQ.context(2);
        sender = context.socket(ZMQ.PUSH);
        receiver = context.socket(ZMQ.PULL);

        sender.setLinger(1000);
        receiver.setLinger(1000);

        reader = new RobotZmqReader();
        reader.setDaemon(true);
    }

    public void bind() {
        sender.bind(BINDING_ADDRESS + ":" + pushPort);
        receiver.bind(BINDING_ADDRESS + ":" + pullPort);

        try {
            Thread.sleep(THREAD_SLEEP_POST_CONNECT_MS);
        } catch (InterruptedException e) {
            logback.error(e.getMessage());
        }

        logback.info("Proxy is binding on ports " + pushPort + " (push) and " + pullPort + " (pull) for robot communication");
        reader.start();
    }

    public void close() {
        isConnected = false;
        sender.close();
        receiver.close();
        context.term();
        logback.info("All sockets of are now closed");
    }

    public boolean send(UnitMessage unitMessage) {
        logback.debug("Sending message to physical device");
        return sender.send(unitMessage.toString());
    }

    public void addMessageListener(MessageListenerInterface messageListenerInterface) {
        messageListeners.add(messageListenerInterface);
    }

    private class RobotZmqReader extends Thread {
        private static final long THREAD_SLEEP_BETWEEN_MSG_MS = 1;

        @Override
        public void run() {
            establishFirstConnection();
            while (isConnected) {
                listenForNewMessage();
                sleepBetweenMessages();
            }
        }

        private void establishFirstConnection() {
            send(new UnitMessage(UnitMessageType.Connection, "ping"));
            while(!isConnected) {
                UnitMessage connectionMessage = listenForNewMessage();
                receivedNewMessage(connectionMessage);
                sleepBetweenMessages();
            }
        }

        private void sleepBetweenMessages() {
            try {
                sleep(THREAD_SLEEP_BETWEEN_MSG_MS);
            } catch (InterruptedException e) {
                logback.error(e.getMessage());
            }
        }

        private UnitMessage listenForNewMessage() {
            String msg = receiver.recvStr(1); // do not block thread waiting for a message
            if (msg != null) {
                logback.debug("Message received: " + msg);
            }
            try {
                UnitMessage unitMessage = UnitMessageBuilder.build(msg);
                receivedNewMessage(unitMessage);
                return unitMessage;
            } catch (UnitMessageException e) {
                logback.debug("Unit Message Exception: " + e.getMessage());
                return null;
            }
        }

        private void receivedNewMessage(UnitMessage unitMessage) {
            for (MessageListenerInterface listener : messageListeners) {
                listener.receivedNewMessage(unitMessage);
            }
        }
    }
}
