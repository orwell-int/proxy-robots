package orwell.proxy;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import orwell.messages.Controller;

public class FakeServer {
    final static Logger logback = LoggerFactory.getLogger(FakeServer.class);

    public FakeServer() {
    }

    public static Controller.Input buildTestInput() {
        Controller.Input.Builder testInput = Controller.Input.newBuilder();
        Controller.Input.Move.Builder testMove = Controller.Input.Move
                .newBuilder();
        testMove.setLeft(50);
        testMove.setRight(0.234);

        testInput.setMove(testMove.build());

        Controller.Input.Fire.Builder testFire = Controller.Input.Fire
                .newBuilder();
        testFire.setWeapon1(false);
        testFire.setWeapon2(true);
        testInput.setFire(testFire);

        return testInput.build();
    }

    static byte[] getZMQmessage(String dest, Controller.Input message) {
        String zMQmessageHeader = dest + " " + "Input" + " ";
        return orwell.proxy.Utils.Concatenate(zMQmessageHeader.getBytes(),
                message.toByteArray());
    }

    public static void main(String[] args) throws Exception {
        FakeServer fakeServer = new FakeServer();
        fakeServer.startServer();
    }

    public void startServer() {
        ZMQ.Context context = ZMQ.context(1);

        // Socket to receive messages on
        ZMQ.Socket receiver = context.socket(ZMQ.PULL);
        ZMQ.Socket sender = context.socket(ZMQ.PUB);
        receiver.setLinger(1000);
        sender.setLinger(1000);
        receiver.bind("tcp://localhost:9000");
        logback.info("Server Receiver created");
        sender.bind("tcp://*:9001");
        logback.info("Server Sender created");

        // Process tasks forever
        while (!Thread.currentThread().isInterrupted()) {
            String zmq_message = receiver.recvStr();
            System.out.flush();
            logback.info("Message received: '" + zmq_message + "'");

            if (0 == zmq_message.compareTo("Banana")) {
                Controller.Input input = buildTestInput();
                logback.info("Building input for test");

                byte[] zmq_input = getZMQmessage("BananaOne", input);
                String str = new String(zmq_input, 0, zmq_input.length);
                logback.info("Message is:" + str);
                // logback.info("raw length: " + str.getBytes().length);
                sender.send(zmq_input);
                try {
                    System.out.println(Controller.Input.parseFrom(
                            input.toByteArray()).toString());
                } catch (InvalidProtocolBufferException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logback.info("Message sent");
            }
        }

        sender.close();
        receiver.close();
        context.term();
    }

}
