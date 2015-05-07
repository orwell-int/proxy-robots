package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * Created by parapampa on 08/03/15.
 */
public class ZmqMessageFramework implements IZmqMessageFramework {

    final static Logger logback = LoggerFactory.getLogger(ZmqMessageFramework.class);
    private final Object rXguard;
    private final ZMQ.Context context;
    private final ZMQ.Socket sender;
    private final ZMQ.Socket receiver;
    protected ArrayList<IZmqMessageListener> zmqMessageListeners;
    protected boolean connected = false;
    protected int nbMessagesSkipped = 0;
    private ZmqReader reader;

    private boolean isSkipIdenticalMessages = false;

    public ZmqMessageFramework(final int senderLinger,
                               final int receiverLinger) {
        logback.info("Constructor -- IN");
        zmqMessageListeners = new ArrayList<>();
        rXguard = new Object();

        context = ZMQ.context(1);
        sender = context.socket(ZMQ.PUSH);
        receiver = context.socket(ZMQ.SUB);

        sender.setLinger(senderLinger);
        receiver.setLinger(receiverLinger);

        setupNewReader();

        logback.info("Constructor -- OUT");
    }

    private void setupNewReader() {
        reader = new ZmqReader();
        reader.setDaemon(true);
    }

    @Override
    public void setSkipIdenticalMessages(final boolean skipIdenticalMessages) {
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
        receiver.subscribe(new String("").getBytes());
        try {
            if (Thread.State.NEW != reader.getState()) {
                logback.error("Reader has already been started once");
                setupNewReader();
            }
            reader.start(); // Start to listen for incoming messages
            connected = true;
        } catch (IllegalThreadStateException e) {
            // TODO Auto-generated catch block
            logback.error(e.getMessage());
        }
        return connected;
    }

    @Override
    public boolean sendZmqMessage(ZmqMessageBOM zmqMessageBOM) {

        //TODO add filter

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
        for (int j = 0; j < zmqMessageListeners.size(); j++) {
            zmqMessageListeners.get(j).receivedNewZmq(zmqMessage);
        }
    }

    @Override
    public void close() {
        logback.info("Stopping communication");
        connected = false;
    }

    private class ZmqReader extends Thread {
        String zmqPreviousMessage = new String();
        ZmqMessageDecoder zmqMessage;

        @Override
        public void run() {
            while (connected) {
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
                    Thread.sleep(10);
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
