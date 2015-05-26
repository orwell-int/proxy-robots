package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created by Michaël Ludmann on 5/25/15.
 */
public class UdpBeaconFinder {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconFinder.class);
    private final int broadcastPort;
    private final int receiverBufferSize = 512;
    private String serverGameIp;
    private String partialPullerAddress;
    private String partialPublisherAddress;
    private String fullPullerAddress;
    private String fullPublisherAddress;
    private boolean isDataReceivedCorrect;
    private final DatagramSocket datagramSocket;

    public UdpBeaconFinder(final DatagramSocket datagramSocket, final int broadcastPort) {
        this.datagramSocket = datagramSocket;
        this.broadcastPort = broadcastPort;
    }

    public static void main(final String[] args) throws Exception {
        final DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(1000);
        final UdpBeaconFinder udpBeaconFinder = new UdpBeaconFinder(datagramSocket, 9080);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());
    }

    /**
     * Find the server using UDP broadcast
     */
    public void startBroadcasting() {
        try {
            datagramSocket.setBroadcast(true);

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
            logback.debug("Broadcast response received from server of ip: " + receivePacket.getAddress().getHostAddress());
            this.serverGameIp = receivePacket.getAddress().getHostAddress();
            //Check if message is correct
            final String message = new String(receivePacket.getData()).trim();
            logback.debug("Message received from server: " + message);
            parsePacketData(receivePacket.getData());

            if(isDataReceivedCorrect && ! isDataReceivedComplete()) {
                this.fullPullerAddress = this.partialPullerAddress.replace("*", this.serverGameIp);
                this.fullPublisherAddress = this.partialPublisherAddress.replace("*", this.serverGameIp);
            }

            datagramSocket.close();

        } catch (final Exception e) {
            logback.error(e.getMessage());
        }
    }

    public boolean hasReceivedCorrectData() {
        return isDataReceivedCorrect;
    }

    public String getServerGameIp() {
        return serverGameIp;
    }

    public String getPublisherAddress() {
        return fullPublisherAddress;
    }

    public String getPullerAddress() {
        return fullPullerAddress;
    }

    /**
     * @param packetData data received from the server's beacon. Example:
     *                   0xA012tcp://*:90010xA112tcp://*:90000x00
     *                   |   | |           |   | |           |
     *                   |   | |           |   | |           checkByteETX (hex value on one byte)
     *                   |   | |           |   | address of publisher (i.e. subscriber address on proxy side)
     *                   |   | |           |   size of server publisher on 8 bits
     *                   |   | |           checkByteSeparator (hex value on one byte)
     *                   |   | address of puller (i.e. push address on proxy side)
     *                   |   size of server puller on 8 bits
     *                   checkByteSTX (hex value on one byte)
     */
    private void parsePacketData(final byte[] packetData) {
        // The 8-bit byte, which is signed in Java, is sign-extended to a 32-bit int.
        // So the casting to int leave us with FFFFFFA0 as a hex value
        // To effectively undo this sign extension, we mask the byte with 0xFF.
        final int checkByteSTX = 0xA0;
        final int byteMask = 0xFF;
        if (checkByteSTX != ((int) packetData[0] & byteMask)) {
            logback.warn("checkByteSTX is not the one expected " + Integer.toHexString((int) packetData[0] & byteMask));
            isDataReceivedCorrect = false;
            return;
        }
        final int pullerSize = (int) packetData[1];
        final int endPuller = 2 + pullerSize;
        partialPullerAddress = new String(Arrays.copyOfRange(packetData, 2, endPuller));
        final int checkByteSeparator = 0xA1;
        if (checkByteSeparator != ((int) packetData[endPuller] & byteMask)) {
            logback.warn("checkByteSeparator is not the one expected" + Integer.toHexString((int) packetData[endPuller] & byteMask));
            isDataReceivedCorrect = false;
            return;
        }
        final int publisherSize = (int) packetData[endPuller + 1];
        final int endPublisher = endPuller + 2 + publisherSize;
        partialPublisherAddress = new String(Arrays.copyOfRange(packetData, endPuller + 2, endPublisher));
        final int checkByteETX = 0x00;
        if (checkByteETX != ((int) packetData[endPublisher] & byteMask)) {
            logback.warn("checkByteETX is not the one expected" + Integer.toHexString((int) packetData[endPublisher] & byteMask));
            isDataReceivedCorrect = false;
            return;
        }

        isDataReceivedCorrect = true;
    }

    /**
     *
     * @return true if partialPullerAddress and partialPublisherAddress do not contain "*" symbol
     */
    private boolean isDataReceivedComplete() {
        return (!partialPullerAddress.contains("*") || !partialPublisherAddress.contains("*"));
    }

    @Override
    public String toString() {
        return "Broadcast info received: \n" +
                "[ServerGameIp]     " + serverGameIp + "\n" +
                "[PartialPullerAddress]    " + partialPullerAddress + "\n" +
                "[PartialPublisherAddress] " + partialPublisherAddress;
    }
}
