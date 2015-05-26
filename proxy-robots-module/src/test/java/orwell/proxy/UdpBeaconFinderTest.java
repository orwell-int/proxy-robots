package orwell.proxy;

import org.easymock.IAnswer;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by Michaël Ludmann on 5/26/15.
 */
@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class UdpBeaconFinderTest {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconFinderTest.class);
    private final static int BROADCAST_PORT = 9080;
    private final static int BROADCAST_TIMEOUT_MS = 5000;
    private final static int RECEIVER_BUFFER_SIZE = 512;

    @TestSubject
    private UdpBeaconFinder udpBeaconFinder;

    @Mock
    private DatagramSocket mockedDatagramSocket;

    @Before
    public void setUp() throws Exception {
        logback.info("IN");
        mockedDatagramSocket = createNiceMock(DatagramSocket.class);
        mockedDatagramSocket.setBroadcast(true);
        mockedDatagramSocket.send((DatagramPacket) anyObject());
        mockedDatagramSocket.close();
        logback.info("OUT");
    }

    /**
     * @return "0xA012tcp://*:90010xA112tcp://*:90000x00" as a byte Array
     */
    private byte[] getTestPacketDataBytes(final int checkByteSTX, final int firstHalfDataSize,
                                          final String firstHalfString, final int checkByteSeparator,
                                          final int secondHalfDataSize, final String secondHalfString,
                                          final int checkByteETX) {
        final int byteMask = 0xFF;
        final byte[] resultBytes = new byte[RECEIVER_BUFFER_SIZE];
        resultBytes[0] = (byte) (checkByteSTX & byteMask);
        resultBytes[1] = (byte) firstHalfDataSize;

        int bytePos = 2;
        for (final byte b : firstHalfString.getBytes()) {
            resultBytes[bytePos] = b;
            bytePos++;
        }
        resultBytes[bytePos] = (byte) (checkByteSeparator & byteMask);
        bytePos++;
        resultBytes[bytePos] = (byte) secondHalfDataSize;
        bytePos++;
        for (final byte b : secondHalfString.getBytes()) {
            resultBytes[bytePos] = b;
            bytePos++;
        }
        resultBytes[bytePos] = (byte) (checkByteETX & byteMask);

        return resultBytes;
    }

    @Test
    public void testStartBroadcasting() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
                                       @Override
                                       public Object answer() throws Throwable {
                                           final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                           datagramPacket.setAddress(InetAddress.getByName("127.0.0.1"));
                                           datagramPacket.setData(getTestPacketDataBytes(0xA0, 12, "tcp://*:9001", 0xA1, 12, "tcp://*:9000", 0x00));
                                           return null;
                                       }
                                   }
        );
        replay(mockedDatagramSocket);

        udpBeaconFinder = new UdpBeaconFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());

        assertTrue(udpBeaconFinder.hasReceivedCorrectData());
        assertEquals("127.0.0.1", udpBeaconFinder.getServerGameIp());
        assertEquals("tcp://127.0.0.1:9001", udpBeaconFinder.getPullerAddress());
        assertEquals("tcp://127.0.0.1:9000", udpBeaconFinder.getPublisherAddress());
    }

    @Test
    public void testStartBroadcasting_badSTX() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
                                       @Override
                                       public Object answer() throws Throwable {
                                           final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                           datagramPacket.setAddress(InetAddress.getByName("127.0.0.1"));
                                           datagramPacket.setData(getTestPacketDataBytes(0xB0, 12, "tcp://*:9001", 0xA1, 12, "tcp://*:9000", 0x00));
                                           return null;
                                       }
                                   }
        );
        replay(mockedDatagramSocket);

        udpBeaconFinder = new UdpBeaconFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());

        assertFalse(udpBeaconFinder.hasReceivedCorrectData());
        assertEquals("127.0.0.1", udpBeaconFinder.getServerGameIp());
        assertNull(udpBeaconFinder.getPullerAddress());
        assertNull(udpBeaconFinder.getPublisherAddress());
    }

    @Test
    public void testStartBroadcasting_badSeparator() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
                                       @Override
                                       public Object answer() throws Throwable {
                                           final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                           datagramPacket.setAddress(InetAddress.getByName("127.0.0.1"));
                                           datagramPacket.setData(getTestPacketDataBytes(0xA0, 12, "tcp://*:9001", 0xB1, 12, "tcp://*:9000", 0x00));
                                           return null;
                                       }
                                   }
        );
        replay(mockedDatagramSocket);

        udpBeaconFinder = new UdpBeaconFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());

        assertFalse(udpBeaconFinder.hasReceivedCorrectData());
        assertEquals("127.0.0.1", udpBeaconFinder.getServerGameIp());
        assertNull(udpBeaconFinder.getPullerAddress());
        assertNull(udpBeaconFinder.getPublisherAddress());
    }

    @Test
    public void testStartBroadcasting_badETX() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
                                       @Override
                                       public Object answer() throws Throwable {
                                           final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                           datagramPacket.setAddress(InetAddress.getByName("127.0.0.1"));
                                           datagramPacket.setData(getTestPacketDataBytes(0xA0, 12, "tcp://*:9001", 0xA1, 12, "tcp://*:9000", 0xB0));
                                           return null;
                                       }
                                   }
        );
        replay(mockedDatagramSocket);

        udpBeaconFinder = new UdpBeaconFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());

        assertFalse(udpBeaconFinder.hasReceivedCorrectData());
        assertEquals("127.0.0.1", udpBeaconFinder.getServerGameIp());
        assertNull(udpBeaconFinder.getPullerAddress());
        assertNull(udpBeaconFinder.getPublisherAddress());
    }

    @Test
    public void testToString() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        replay(mockedDatagramSocket);

        udpBeaconFinder = new UdpBeaconFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS);
        udpBeaconFinder.startBroadcasting();
        logback.info(udpBeaconFinder.toString());

        assertEquals("Broadcast info received: \n" +
                "[ServerGameIp]     null\n" +
                "[PartialPullerAddress]    null\n" +
                "[PartialPublisherAddress] null", udpBeaconFinder.toString());
    }
}
