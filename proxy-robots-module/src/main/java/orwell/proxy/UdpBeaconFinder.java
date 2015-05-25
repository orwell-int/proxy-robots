package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by Michaël Ludmann on 5/25/15.
 */
public class UdpBeaconFinder {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconFinder.class);
    private final int broadcastPort;
    private String serverGameIp;
    private int pushPort;
    private int subPort;

    public UdpBeaconFinder(final int broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    public void startBroadcasting() {
        DatagramSocket datagramSocket;
        InetAddress inetAddress;

        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);

            final byte[] sendData = "NINI".getBytes();
            //Try the 255.255.255.255 first
//            try {
//                final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), broadcastPort);
//                datagramSocket.send(sendPacket);
//                logback.info(">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
//            } catch (final Exception e) {
//            }

            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = interfaces.nextElement();

                if(networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                for (final InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    final InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (null == broadcast) {
                        continue;
                    }

                    try {
                        final String ipPing = broadcast.getHostAddress().toString();

                        final byte[] ipPingBytes = ipPing.getBytes();
                        final DatagramPacket datagramPacket = new DatagramPacket(ipPingBytes, ipPingBytes.length, broadcast, broadcastPort);
                        datagramSocket.send(datagramPacket);
                    } catch (final Exception e) {
                        logback.error(e.getMessage());
                    }

                    logback.info(">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }

            }

            logback.info(">>> Done looping over all network interfaces. Now waiting for a reply!");

            final byte[] recvBuf = new byte[512];
            final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            datagramSocket.receive(receivePacket);

            logback.info(">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
            this.serverGameIp = receivePacket.getAddress().getHostAddress();
            //Check if message is correct
            final String message = new String(receivePacket.getData()).trim();
            logback.debug(">>> Message received: " + message);
            

        } catch (final Exception e) {
            logback.error(e.getMessage());
        }
    }

    public String getServerGameIp() {
        return serverGameIp;
    }

    public int getPushPort() {
        return pushPort;
    }

    public int getSubPort() {
        return subPort;
    }

    @Override
    public String toString() {
        return "Broadcast info received: \n" +
                "ServerGameIp: " + serverGameIp + "\n" +
                "PushPort: " + pushPort + "\n" +
                "SubPort: " + subPort;
    }

    public static void main(final String[] args) throws Exception {
        final UdpBeaconFinder udpBeaconFinder = new UdpBeaconFinder(9080);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());
    }
}
