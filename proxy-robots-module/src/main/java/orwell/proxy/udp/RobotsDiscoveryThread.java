package orwell.proxy.udp;

import javassist.bytecode.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by Michaël Ludmann on 26/02/17.
 */
public class RobotsDiscoveryThread implements Runnable {
    private final static Logger logback = LoggerFactory.getLogger(RobotsDiscoveryThread.class);

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP traffic that is destined for this port
            DatagramSocket socket = new DatagramSocket(9081, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                logback.info(">>>Ready to receive broadcast packets from robots!");

                //Receive a packet
                byte[] receiverBuffer = new byte[15000];
                DatagramPacket packet = new DatagramPacket(receiverBuffer, receiverBuffer.length);
                socket.receive(packet);

                //Packet received
                logback.info(">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                logback.info(">>>Packet received; data: " + new String(packet.getData()));

                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();
                if (message.equals("DISCOVER_PROXY-ROBOTS_REQUEST")) {
                    final int checkByteSTX = 0xA2;
                    final int checkByteSeparator = 0xA3;
                    final int checkByteETX = 0xA4;
                    final String robotPushAddress = "tcp://IP:10001";
                    final String robotPullAddress = "tcp://IP:10000";
                    byte[] sendData = new byte[5 + robotPushAddress.length() + robotPullAddress.length()];
                    int index = 0;
                    sendData[index++] = (byte) checkByteSTX;
                    sendData[index++] = (byte) robotPushAddress.length();
                    System.arraycopy(robotPushAddress.getBytes(), 0, sendData, index, robotPushAddress.length());
                    index += robotPushAddress.length();
                    sendData[index++] = (byte) checkByteSeparator;
                    sendData[index++] = (byte) robotPullAddress.length();
                    System.arraycopy(robotPullAddress.getBytes(), 0, sendData, index, robotPullAddress.length());
                    index += robotPullAddress.length();
                    sendData[index++] = (byte) checkByteETX;
                    //Send a response
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);

                    logback.info(">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
                }
            }
        } catch (IOException ex) {
            logback.error(ex.toString());
        }
    }

    public static RobotsDiscoveryThread getInstance() {
        return RobotsDiscoveryThreadHolder.INSTANCE;
    }

    private static class RobotsDiscoveryThreadHolder {

        private static final RobotsDiscoveryThread INSTANCE = new RobotsDiscoveryThread();
    }
}
