package orwell.proxy.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private int maxAttemptsNumber;
    private int attemptsPerformed = 0;
    private String pushAddress;
    private String subscribeAddress;

    public UdpBeaconFinder(final DatagramSocket datagramSocket,
                           final int broadcastPort,
                           final UdpBeaconDecoder udpBeaconDecoder) {
        this.datagramSocket = datagramSocket;
        this.broadcastPort = broadcastPort;
        this.udpBeaconDecoder = udpBeaconDecoder;
        maxAttemptsNumber = 1;
    }

    /**
     * Change number of attempts to find the UDP beacon server
     * Default value is set to 1
     * @param maxAttemptsNumber
     */
    public void setMaxAttemptsNumber(final int maxAttemptsNumber) {
        if (0 > maxAttemptsNumber) {
            logback.warn("Udp broadcasting attempts number cannot be less than 0");
        } else {
            this.maxAttemptsNumber = maxAttemptsNumber;
        }
    }

    public int getNumberOfPerformedAttempts() {
        return attemptsPerformed;
    }

    /**
     * @return true if we did not get any meaningful response from server
     * and have not reached max allowed attempts number
     */
    private boolean shouldTryToFindBeacon() {
        if (null == udpBeaconDecoder) {
            return attemptsPerformed < maxAttemptsNumber;
        }
        return (!udpBeaconDecoder.hasReceivedCorrectData() &&
                attemptsPerformed < maxAttemptsNumber);
    }

    /**
     * Find the server using UDP broadcast and fill data fields
     */
    public void broadcastAndGetServerAddress() {
        try {
            datagramSocket.setBroadcast(true);
            while (shouldTryToFindBeacon()) {
                logback.info("Trying to find UDP beacon, attempt [" + new Integer(attemptsPerformed+1) + "]");
                // Broadcast the message over all the network interfaces
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                while (interfaces.hasMoreElements()) {
                    final NetworkInterface networkInterface = interfaces.nextElement();
                    sendBroadcastToInterface(networkInterface);
                }

                logback.info("Done looping over all network interfaces. Now waiting for a reply!");
                waitForServerResponse(datagramSocket);

                attemptsPerformed++;
            }
            fillFoundAddressFields();
            datagramSocket.close();


        } catch (final Exception e) {
            logback.error(e.getMessage());
        }
    }

    private void sendBroadcastToInterface(final NetworkInterface networkInterface) throws SocketException {
        if (networkInterface.isLoopback() || !networkInterface.isUp()) {
            return; // Do not broadcast to the loopback interface
        }

        for (final InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
            final InetAddress broadcastAddress = interfaceAddress.getBroadcast();
            if (null != broadcastAddress) {
                logback.info("Trying to send broadcast package on interface: " + networkInterface.getDisplayName());
                sendBroadcastPackageToAddress(broadcastAddress);
            }
        }
    }

    private void sendBroadcastPackageToAddress(final InetAddress broadcastAddress) {
        try {
            final String ipPing = broadcastAddress.getHostAddress();

            final byte[] ipPingBytes = ipPing.getBytes();
            final DatagramPacket datagramPacket = new DatagramPacket(ipPingBytes, ipPingBytes.length, broadcastAddress, broadcastPort);
            datagramSocket.send(datagramPacket);
        } catch (final Exception e) {
            logback.error(e.getMessage());
        }

        logback.info("Request packet sent to: " + broadcastAddress.getHostAddress());
    }

    private void waitForServerResponse(final DatagramSocket datagramSocket) throws IOException {
        final byte[] recvBuf = new byte[receiverBufferSize];
        final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

        try {
            datagramSocket.receive(receivePacket);
            udpBeaconDecoder.parseFrom(receivePacket);
        } catch (final SocketTimeoutException e) {
            logback.info("Datagram socket received timeout, continue...");
        }
    }

    private void fillFoundAddressFields() {
        if (hasFoundServer()) {
            this.pushAddress = udpBeaconDecoder.getPushAddress();
            this.subscribeAddress = udpBeaconDecoder.getSubscribeAddress();
        }
    }

    /**
     * @return null if broadcast was not called first
     * otherwise return puller address of the server (push on proxy side)
     */
    public String getPushAddress() {
        return pushAddress;
    }

    /**
     * @return null if broadcast was not called first
     * otherwise return publisher address of the server (subscribe on proxy side)
     */
    public String getSubscribeAddress() {
        return subscribeAddress;
    }

    /**
     * @return true if UdpBeaconFinder has found the server and it returned correct data
     */
    public boolean hasFoundServer() {
        return (null != udpBeaconDecoder) && udpBeaconDecoder.hasReceivedCorrectData();
    }
}
