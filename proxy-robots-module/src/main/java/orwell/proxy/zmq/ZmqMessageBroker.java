package orwell.proxy.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 08/03/15.
 */
public class ZmqMessageBroker implements IZmqMessageBroker {

    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageBroker.class);
    private static final long THREAD_SLEEP_MS = 10;
    private final Object rXguard;
    private final ZMQ.Context context;
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    final private ArrayList<IFilter> filterList;
    private final ArrayList<IZmqMessageListener> zmqMessageListeners;
    private boolean isConnected = false;
    private int nbMessagesSkipped = 0;
    private ZmqReader reader;
    private boolean isSkipIdenticalMessages = false;

    public ZmqMessageBroker(final int senderLinger,
                            final int receiverLinger,
                            final ArrayList<IFilter> filterList) {
        logback.info("Constructor -- IN");
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

    private void setupNewReader() {
        reader = new ZmqReader();
        reader.setDaemon(true);
    }

    @Override
    public void setSkipIncomingIdenticalMessages(final boolean skipIdenticalMessages) {
        isSkipIdenticalMessages = skipIdenticalMessages;
    }

    @Override
    public boolean connectToServer(final String serverIp,
                                   final int pushPort,
                                   final int subPort) {
        sender.connect("tcp://" + serverIp + ":"
                + pushPort);
        logback.info("ProxyRobots Sender created");
        receiver.connect("tcp://" + serverIp + ":"
                + subPort);
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

    private void receivedNewZmqMessage(final ZmqMessageDecoder zmqMessage) {
        logback.debug("Received New ZMQ Message : " + zmqMessage.getMessageType());
        for (final IZmqMessageListener zmqMessageListener : zmqMessageListeners) {
            zmqMessageListener.receivedNewZmq(zmqMessage);
        }
    }

    @Override
    public void close() {
        logback.info("Stopping communication");
        isConnected = false;
    }

    @Override
    public boolean isConnectedToServer() {
        return isConnected;
    }

    public int getNbMessagesSkipped() {
        return nbMessagesSkipped;
    }

    private class ZmqReader extends Thread {
        String zmqPreviousMessage = "";
        ZmqMessageDecoder zmqMessage;

        @Override
        public void run() {
            logback.info("ZmqReader has been started");
            while (isConnected) {
                final byte[] raw_zmq_message = receiver.recv(ZMQ.NOBLOCK);
                if (null != raw_zmq_message) {
                    synchronized (rXguard) {
                        zmqMessage = new ZmqMessageDecoder(raw_zmq_message);

                        // We do not want to uselessly flood the robot
                        if (isSkipIdenticalMessages && 0 == zmqMessage.getZmqMessageString().compareTo(zmqPreviousMessage)) {
                            nbMessagesSkipped++;
                            logback.debug("Current zmq message identical to previous zmq message (already done " +
                                    nbMessagesSkipped + " time(s) for this message)");
                        } else {
                            nbMessagesSkipped = 0;
                            receivedNewZmqMessage(zmqMessage);
                        }
                        zmqPreviousMessage = zmqMessage.getZmqMessageString();
                    }
                }
                try {
                    // This is performed to avoid high CPU consumption
                    Thread.sleep(THREAD_SLEEP_MS);
                } catch (final InterruptedException e) {
                    logback.error("ZmqReader thread sleep exception: " + e.getMessage());
                }
            }
            sender.close();
            receiver.close();
            context.term();
            Thread.yield();
            logback.info("Communication stopped");
        }
    }
}
