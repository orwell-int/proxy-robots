package orwell.proxy.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Michael Ludmann on 08/03/15.
 */
public class ServerGameMessageBroker implements IServerGameMessageBroker {

    private final static Logger logback = LoggerFactory.getLogger(ServerGameMessageBroker.class);
    private static final long THREAD_SLEEP_MS = 10;
    private final Object rXguard;
    private final ZMQ.Context context;
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    final private ArrayList<IFilter> filterList;
    private final ArrayList<IZmqMessageListener> zmqMessageListeners;
    private final long socketTimeoutMs;
    private final boolean isSocketTimeoutSet;
    private volatile boolean isConnected = false;
    private int nbSuccessiveSameMessages = 0;
    private ZmqReader reader;
    private boolean isSkipIdenticalMessages = false;
    private volatile long baseTimeMs;
    private int nbBadMessages;

    public ServerGameMessageBroker(final long receiveTimeoutMs,
                                   final int senderLinger,
                                   final int receiverLinger,
                                   final ArrayList<IFilter> filterList) {
        logback.info("Constructor -- IN");
        socketTimeoutMs = receiveTimeoutMs;
        isSocketTimeoutSet = 0 < receiveTimeoutMs;
        zmqMessageListeners = new ArrayList<>();
        rXguard = new Object();

        context = ZMQ.context(1);
        sender = context.socket(ZMQ.PUSH);
        receiver = context.socket(ZMQ.SUB);

        sender.setLinger(senderLinger);
        receiver.setLinger(receiverLinger);

        setupNewReader();

        this.filterList = filterList;

        logback.info("Constructor -- OUT");
    }

    public ServerGameMessageBroker(final long receiveTimeoutMs,
                                   final int senderLinger,
                                   final int receiverLinger) {
        this(receiveTimeoutMs, senderLinger, receiverLinger, null);
    }

    private void setupNewReader() {
        reader = new ZmqReader();
        reader.setDaemon(true);
    }

    @Override
    public void setSkipIncomingIdenticalMessages(final boolean skipIdenticalMessages) {
        isSkipIdenticalMessages = skipIdenticalMessages;
    }

    @Override
    public boolean connectToServer(final String pushAddress,
                                   final String subscribeAddress) {
        sender.connect(pushAddress);
        logback.info("ProxyRobots Sender created");
        receiver.connect(subscribeAddress);
        logback.info("ProxyRobots Receiver created");
        receiver.subscribe("".getBytes());
        try {
            if (Thread.State.NEW != reader.getState()) {
                logback.error("Reader has already been started once");
                setupNewReader();
            }
            reader.start(); // Start to listen for incoming messages
            isConnected = true;
        } catch (final IllegalThreadStateException e) {
            logback.error(e.getMessage());
        }
        return isConnected;
    }

    @Override
    public boolean sendZmqMessage(ZmqMessageBOM zmqMessageBOM) {

        // We apply the filters sequentially
        if (null != this.filterList) {
            for (final IFilter filter : this.filterList) {
                zmqMessageBOM = filter.getFilteredMessage(zmqMessageBOM);
            }
        }

        if (zmqMessageBOM.isEmpty())
            return false;
        else
            return sender.send(zmqMessageBOM.getZmqMessageBytes(), 0);
    }

    @Override
    public void addZmqMessageListener(final IZmqMessageListener zmqMsgListener) {
        zmqMessageListeners.add(zmqMsgListener);
    }

    @Override
    public void close() {
        logback.info("Closing ZMQ message broker");
        isConnected = false;
    }

    @Override
    public boolean isConnectedToServer() {
        return isConnected;
    }

    public int getNbSuccessiveMessagesSkipped() {
        return nbSuccessiveSameMessages;
    }

    public int getNbBadMessages() {
        return nbBadMessages;
    }

    private boolean hasReceivedTimeoutExpired() {
        return isSocketTimeoutSet &&
                (System.currentTimeMillis() - baseTimeMs > socketTimeoutMs);
    }

    private class ZmqReader extends Thread {
        ZmqMessageBOM previousZmqMessage;
        ZmqMessageBOM newZmqMessage;

        @Override
        public void run() {
            logback.info("ZmqReader has been started");
            baseTimeMs = System.currentTimeMillis();
            while (isConnected && !hasReceivedTimeoutExpired()) {
                final byte[] raw_zmq_message = receiver.recv(ZMQ.NOBLOCK);
                if (null != raw_zmq_message) {
                    synchronized (rXguard) {
                        handleRawZmqMessage(raw_zmq_message);
                    }
                }
                try {
                    // This is performed to avoid high CPU consumption
                    Thread.sleep(THREAD_SLEEP_MS);
                } catch (final InterruptedException e) {
                    logback.error("ZmqReader thread sleep exception: " + e.getMessage());
                }
            }
            terminateZmqReader();
        }

        private void handleRawZmqMessage(final byte[] raw_zmq_message) {
            baseTimeMs = System.currentTimeMillis();
            try {
                newZmqMessage = ZmqMessageBOM.parseFrom(raw_zmq_message);

                // We do not want to uselessly flood the robot
                if (isSkipIdenticalMessages && newZmqMessage.equals(previousZmqMessage)) {
                    onReceivedIdenticalMessage();
                } else {
                    onReceivedNewZmqMessage(newZmqMessage);
                }
                previousZmqMessage = newZmqMessage;
            } catch (final ParseException e) {
                onReceivedBadMessage(e);
            }
        }

        private void terminateZmqReader() {
            if (hasReceivedTimeoutExpired()) {
                logback.info("Communication stopped: socket received timeout after " + socketTimeoutMs + "ms");
            }
            isConnected = false;
            sender.close();
            receiver.close();
            context.term();
            Thread.yield();
            logback.info("All sockets are now closed");
        }

        private void onReceivedIdenticalMessage() {
            nbSuccessiveSameMessages++;
            logback.debug("Current zmq message identical to previous zmq message (already done " +
                    nbSuccessiveSameMessages + " time(s) for this message)");
        }

        private void onReceivedNewZmqMessage(final ZmqMessageBOM zmqMessageBOM) {
            nbSuccessiveSameMessages = 0;
            logback.debug("Received New ZMQ Message : " + zmqMessageBOM.getMessageType());
            for (final IZmqMessageListener zmqMessageListener : zmqMessageListeners) {
                zmqMessageListener.receivedNewZmq(zmqMessageBOM);
            }
        }

        private void onReceivedBadMessage(final ParseException e) {
            nbSuccessiveSameMessages = 0;
            nbBadMessages++;
            logback.warn("Message received ignored: " + e.getMessage());
        }
    }
}
