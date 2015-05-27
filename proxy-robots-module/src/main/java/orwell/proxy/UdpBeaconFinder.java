package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * Created by Michaël Ludmann on 5/25/15.
 */
public class UdpBeaconFinder {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconFinder.class);
    private final int broadcastPort;
    private final int receiverBufferSize = 512;
    private final DatagramSocket datagramSocket;
    private final UdpBeaconDecoder udpBeaconDecoder;
    private int maxAttemptsNumber = 1;

    public UdpBeaconFinder(final DatagramSocket datagramSocket, final int broadcastPort, final UdpBeaconDecoder udpBeaconDecoder) {
        this.datagramSocket = datagramSocket;
        this.broadcastPort = broadcastPort;
        this.udpBeaconDecoder = udpBeaconDecoder;
    }

    public static void main(final String[] args) throws Exception {
        final DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(1000);
        final UdpBeaconFinder udpBeaconFinder = new UdpBeaconFinder(datagramSocket, 9080, new UdpBeaconDecoder());
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());
    }

    public void setMaxAttemptsNumber(final int maxAttemptsNumber) {
        if (1 > maxAttemptsNumber) {
            logback.warn("Udp broadcasting attempts number cannot be less than 1");
        } else {
            this.maxAttemptsNumber = maxAttemptsNumber;
        }
    }

    private boolean shouldTryToFindBeacon(final int currentAttempt) {
        if (null == udpBeaconDecoder) {
            return currentAttempt <= maxAttemptsNumber;
        }
        return (!udpBeaconDecoder.hasReceivedCorrectData() &&
                currentAttempt <= maxAttemptsNumber);
    }

    /**
     * Find the server using UDP broadcast
     */
    public void startBroadcasting() {
        try {
            datagramSocket.setBroadcast(true);
            int currentAttempt = 1;
            while (shouldTryToFindBeacon(currentAttempt)) {
                logback.info("Trying to find UDP beacon, attempt [" + currentAttempt + "]");
                // Broadcast the message over all the network interfaces
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface networkInterface = interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Do not broadcast to the loopback interface
                    }

                    for (final InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        final InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (null == broadcast) {
                            continue;
                        }

                        // Send the broadcast package
                        try {
                            final String ipPing = broadcast.getHostAddress();

                            final byte[] ipPingBytes = ipPing.getBytes();
                            final DatagramPacket datagramPacket = new DatagramPacket(ipPingBytes, ipPingBytes.length, broadcast, broadcastPort);
                            datagramSocket.send(datagramPacket);
                        } catch (final Exception e) {
                            logback.error(e.getMessage());
                        }

                        logback.info("Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    }
                }

                logback.info("Done looping over all network interfaces. Now waiting for a reply!");

                // Wait for a response
                final byte[] recvBuf = new byte[receiverBufferSize];
                final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                datagramSocket.receive(receivePacket);

                // Response received
                udpBeaconDecoder.parseFrom(receivePacket);
                currentAttempt++;
            }
            datagramSocket.close();

        } catch (final Exception e) {
            logback.error(e.getMessage());
        }
    }
}
