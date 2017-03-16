package orwell.proxy.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.util.Arrays;

public class UdpBroadcastDataDecoder {
    private final static Logger logback = LoggerFactory.getLogger(UdpBroadcastDataDecoder.class);
    private boolean isPacketDataCorrect;
    private String serverGameIp;
    private String partialPullerAddress;
    private String partialPublisherAddress;
    private String pushAddress;
    private String subscribeAddress;

    public UdpBroadcastDataDecoder() {

    }

    public void parseFrom(final DatagramPacket datagramPacket) {
        logback.debug("Broadcast response received from server of ip: " + datagramPacket.getAddress().getHostAddress());
        this.serverGameIp = datagramPacket.getAddress().getHostAddress();
        //Check if message is correct
        logback.debug("Message received from server: " + new String(datagramPacket.getData()).trim());

        parsePacketData(datagramPacket.getData());

        // Transform the address in complete ones that can be used by the proxy later on
        // Invert their names, to go from a server point of view (puller, publisher) to
        // a client point of view (pusher, subscriber)
        if (isPacketDataCorrect && !isDataReceivedComplete()) {
            this.pushAddress = this.partialPullerAddress.replace("*", this.serverGameIp);
            this.subscribeAddress = this.partialPublisherAddress.replace("*", this.serverGameIp);
        } else {
            isPacketDataCorrect = false;
        }
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
            isPacketDataCorrect = false;
            return;
        }
        final int pullerSize = (int) packetData[1];
        final int endPuller = 2 + pullerSize;
        partialPullerAddress = new String(Arrays.copyOfRange(packetData, 2, endPuller));
        final int checkByteSeparator = 0xA1;
        if (checkByteSeparator != ((int) packetData[endPuller] & byteMask)) {
            logback.warn("checkByteSeparator is not the one expected" + Integer.toHexString((int) packetData[endPuller] & byteMask));
            isPacketDataCorrect = false;
            return;
        }
        final int publisherSize = (int) packetData[endPuller + 1];
        final int endPublisher = endPuller + 2 + publisherSize;
        partialPublisherAddress = new String(Arrays.copyOfRange(packetData, endPuller + 2, endPublisher));
        final int checkByteETX = 0x00;
        if (checkByteETX != ((int) packetData[endPublisher] & byteMask)) {
            logback.warn("checkByteETX is not the one expected" + Integer.toHexString((int) packetData[endPublisher] & byteMask));
            isPacketDataCorrect = false;
            return;
        }

        isPacketDataCorrect = true;
    }

    /**
     * @return true if partialPullerAddress and partialPublisherAddress do not contain "*" symbol
     */
    private boolean isDataReceivedComplete() {
        return (!partialPullerAddress.contains("*") || !partialPublisherAddress.contains("*"));
    }

    public boolean hasReceivedCorrectData() {
        return isPacketDataCorrect;
    }

    public String getServerGameIp() {
        return serverGameIp;
    }

    public String getSubscribeAddress() {
        return subscribeAddress;
    }

    public String getPushAddress() {
        return pushAddress;
    }

    @Override
    public String toString() {
        return "UdpBroadcastDataDecoder values decoded: \n" +
                "[ServerGameIp]     " + serverGameIp + "\n" +
                "[PartialPullerAddress]    " + partialPullerAddress + "\n" +
                "[PartialPublisherAddress] " + partialPublisherAddress;
    }
}
