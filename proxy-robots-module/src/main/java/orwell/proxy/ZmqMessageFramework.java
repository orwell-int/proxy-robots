package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by parapampa on 08/03/15.
 */
public class ZmqMessageFramework {

    final static Logger logback = LoggerFactory.getLogger(ZmqMessageFramework.class);

    protected ArrayList<IZmqMessageListener> zmqMessageListeners;
    protected boolean connected = false;
    private Object rXguard;
    protected int nbMessagesSkiped = 0;

    private ZMQ.Context context;
    private ZMQ.Socket sender;
    private ZMQ.Socket receiver;
    private ZmqReader reader;

    private boolean isSkipIdenticalMessages = false;

    public ZmqMessageFramework(int senderLinger, int receiverLinger) {
        logback.info("Constructor -- IN");
        zmqMessageListeners = new ArrayList<IZmqMessageListener>();
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

    /**
     * Decide whether to handle two identical successive messages or to ignore the second
     * @param skipIdenticalMessages : if true, the second identical message (in a row) will be ignored
     */
    public void setSkipIdenticalMessages(boolean skipIdenticalMessages) {
        isSkipIdenticalMessages = skipIdenticalMessages;
    }

    public boolean connectToServer(String serverIp,
                                   int pushPort,
                                   int subPort) {
        sender.connect("tcp://" + serverIp + ":"
                + pushPort);
        logback.info("ProxyRobots Sender created");
        receiver.connect("tcp://" + serverIp + ":"
                + subPort);
        logback.info("ProxyRobots Receiver created");
        receiver.subscribe(new String("").getBytes());
        try {
            if (reader.getState() != Thread.State.NEW) {
                logback.error("Reader has already been started once");
                setupNewReader();
            }
            reader.start(); // Start to listen for incoming messages
            connected = true;
        } catch (IllegalThreadStateException e) {
            // TODO Auto-generated catch block
            logback.error(e.toString());
        }
        return connected;
    }

    public boolean sendZmqMessage(EnumMessageType msgType,
                               String routingID,
                               byte[] msgBytes) {
        String zmqMessageHeader = routingID + " ";
        switch (msgType) {
            case REGISTER:
                zmqMessageHeader += "Register ";
                break;
            case SERVER_ROBOT_STATE:
                zmqMessageHeader += "ServerRobotState ";
                break;
            default:

        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(zmqMessageHeader.getBytes());
            outputStream.write(msgBytes);
        } catch (IOException e) {
            logback.error("SendZmqMessage " + e.toString());
        }
        byte[] zmqMessage = outputStream.toByteArray();
        return sender.send(zmqMessage, 0);
    }

    public void addZmqMessageListener(IZmqMessageListener zmqMsgListener) {
        zmqMessageListeners.add(zmqMsgListener);
    }

    private class ZmqReader extends Thread {
        String zmqPreviousMessage = new String();
        ZmqMessageWrapper zmqMessage;

        @Override
        public void run() {
            while (connected) {
                byte[] raw_zmq_message = receiver.recv(ZMQ.NOBLOCK);
                if (null != raw_zmq_message) {
                    synchronized (rXguard) {
                        zmqMessage = new ZmqMessageWrapper(raw_zmq_message);

                        // We do not want to uselessly flood the robot
                        if (isSkipIdenticalMessages && zmqMessage.getZmqMessageString().compareTo(zmqPreviousMessage) == 0) {
                            nbMessagesSkiped++;
                            logback.debug("Current zmq message identical to previous zmq message (already done " +
                                    nbMessagesSkiped + " time(s) for this message)");
                        } else {
                            nbMessagesSkiped = 0;
                            receivedNewZmqMessage(zmqMessage);
                        }
                        zmqPreviousMessage = zmqMessage.getZmqMessageString();
                    }
                }
            }
            sender.close();
            receiver.close();
            context.term();
            Thread.yield();
            logback.info("Communication stopped");
        }
    }

    private void receivedNewZmqMessage(ZmqMessageWrapper zmqMessage) {
        logback.debug("Received New ZMQ Message : " + zmqMessage.getMessageType());
        for (int j = 0; j < zmqMessageListeners.size(); j++) {
            zmqMessageListeners.get(j).receivedNewZmq(zmqMessage);
        }
    }

    public void close() {
        logback.info("Stopping communication");
        connected = false;
    }
}
