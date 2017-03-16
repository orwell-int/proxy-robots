package orwell.proxy.udp;

import junit.framework.AssertionFailedError;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
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
public class UdpBeaconTest {
    private final static Logger logback = LoggerFactory.getLogger(UdpBeaconTest.class);
    private final static int BROADCAST_TIMEOUT_MS = 5000;
    private final static int RECEIVER_BUFFER_SIZE = 512;
    private final static int ADDRESS_SIZE = 12;
    private final static int BAD_BYTE = 0xB0;
    private final static int CHECK_BYTE_STX = 0xA0;
    private final static int CHECK_BYTE_SEPARATOR = 0xA1;
    private final static int CHECK_BYTE_ETX = 0x00;
    private final static String IP_TEST = "127.0.0.1";
    private static final String PARTIAL_PUSH_PORT = "tcp://*:9001";
    private static final String PARTIAL_PUB_PORT = "tcp://*:9000";
    private static final String TCP_PUSH_ADDRESS = "tcp://127.0.0.1:9001";
    private static final String TCP_SUBSCRIBE_ADDRESS = "tcp://127.0.0.1:9000";

    @TestSubject
    private UdpServerGameFinder udpServerGameFinder;
    private UdpBroadcastDataDecoder udpBroadcastDataDecoder;

    @Mock
    private DatagramSocket mockedDatagramSocket;

    @Before
    public void setUp() throws Exception {
        logback.debug(">>>>>>>>> IN");
        mockedDatagramSocket = createNiceMock(DatagramSocket.class);
        mockedDatagramSocket.setBroadcast(true);
        mockedDatagramSocket.send((DatagramPacket) anyObject());
        mockedDatagramSocket.close();

        udpBroadcastDataDecoder = new UdpBroadcastDataDecoder();
    }

    /**
     * @return "CHECK_BYTE_STX12tcp://*:9001CHECK_BYTE_SEPARATOR12tcp://*:9000CHECK_BYTE_ETX" as a byte Array
     */
    private byte[] getTestPacketDataBytes(final int CHECkByteSTX, final int firstHalfDataSize,
                                          final String firstHalfString, final int CHECkByteSeparator,
                                          final int secondHalfDataSize, final String secondHalfString,
                                          final int CHECkByteETX) {
        final int byteMask = 0xFF;
        final byte[] resultBytes = new byte[RECEIVER_BUFFER_SIZE];
        resultBytes[0] = (byte) (CHECkByteSTX & byteMask);
        resultBytes[1] = (byte) firstHalfDataSize;

        int bytePos = 2;
        for (final byte b : firstHalfString.getBytes()) {
            resultBytes[bytePos] = b;
            bytePos++;
        }
        resultBytes[bytePos] = (byte) (CHECkByteSeparator & byteMask);
        bytePos++;
        resultBytes[bytePos] = (byte) secondHalfDataSize;
        bytePos++;
        for (final byte b : secondHalfString.getBytes()) {
            resultBytes[bytePos] = b;
            bytePos++;
        }
        resultBytes[bytePos] = (byte) (CHECkByteETX & byteMask);

        return resultBytes;
    }

    @Test
    public void testStartBroadcasting() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(CHECK_BYTE_STX, ADDRESS_SIZE, PARTIAL_PUSH_PORT, CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket);
        assertTrue(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertEquals(TCP_PUSH_ADDRESS, udpBroadcastDataDecoder.getPushAddress());
        assertEquals(TCP_SUBSCRIBE_ADDRESS, udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testStartBroadcasting_badSTX() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(BAD_BYTE, ADDRESS_SIZE, PARTIAL_PUSH_PORT, CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket);
        assertFalse(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertNull(udpBroadcastDataDecoder.getPushAddress());
        assertNull(udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testStartBroadcasting_badSeparator() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(CHECK_BYTE_STX, ADDRESS_SIZE, PARTIAL_PUSH_PORT, BAD_BYTE, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket);
        assertFalse(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertNull(udpBroadcastDataDecoder.getPushAddress());
        assertNull(udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testStartBroadcasting_badETX() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(CHECK_BYTE_STX, ADDRESS_SIZE, PARTIAL_PUSH_PORT, CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, BAD_BYTE));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket);
        assertFalse(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertNull(udpBroadcastDataDecoder.getPushAddress());
        assertNull(udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testStartBroadcasting_badDataFormat_LengthError() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(CHECK_BYTE_STX, 11, PARTIAL_PUSH_PORT, CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket);
        assertFalse(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertNull(udpBroadcastDataDecoder.getPushAddress());
        assertNull(udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testStartBroadcasting_badDataFormat_StarMissing() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(CHECK_BYTE_STX, ADDRESS_SIZE, "tcp://.:9001", CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket);
        assertFalse(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertNull(udpBroadcastDataDecoder.getPushAddress());
        assertNull(udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testToString() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        assertEquals("UdpBroadcastDataDecoder values decoded: \n" +
                "[ServerGameIp]     null\n" +
                "[PartialPullerAddress]    null\n" +
                "[PartialPublisherAddress] null", udpBroadcastDataDecoder.toString());
    }

    @Test
    public void testSetMaxAttemptsNumber_twoAttempts() throws Exception {
        mockedDatagramSocket.receive((DatagramPacket) anyObject());

        // First return a badly formatted packet
        expectLastCall()
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(BAD_BYTE, ADDRESS_SIZE, PARTIAL_PUSH_PORT, CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                ) // Then return a good one at the second try
                .andAnswer(new IAnswer<Object>() {
                               @Override
                               public Object answer() throws Throwable {
                                   final DatagramPacket datagramPacket = (DatagramPacket) getCurrentArguments()[0];
                                   datagramPacket.setAddress(InetAddress.getByName(IP_TEST));
                                   datagramPacket.setData(getTestPacketDataBytes(CHECK_BYTE_STX, ADDRESS_SIZE, PARTIAL_PUSH_PORT, CHECK_BYTE_SEPARATOR, ADDRESS_SIZE, PARTIAL_PUB_PORT, CHECK_BYTE_ETX));
                                   return null;
                               }
                           }
                );
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.setMaxAttemptsNumber(2);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket); // Check that 2 tries where performed
        assertTrue(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertEquals(IP_TEST, udpBroadcastDataDecoder.getServerGameIp());
        assertEquals("tcp://127.0.0.1:9001", udpBroadcastDataDecoder.getPushAddress());
        assertEquals("tcp://127.0.0.1:9000", udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @Test
    public void testSetMaxAttemptsNumber_zeroAttempt() throws Exception {
        // We reset the mock, since one call registered during setup
        // won't be made in this scenario
        mockedDatagramSocket = createNiceMock(DatagramSocket.class);
        mockedDatagramSocket.setBroadcast(true);

        // We will never call this method
        mockedDatagramSocket.send((DatagramPacket) anyObject());
        expectLastCall().andThrow(new AssertionFailedError("UDP beacon finder should not try to find the beacon")).anyTimes();

        mockedDatagramSocket.close();

        // We will never call this method
        mockedDatagramSocket.receive((DatagramPacket) anyObject());
        expectLastCall().andThrow(new AssertionFailedError("UDP beacon finder should not receive data from beacon")).anyTimes();

        // Save the scenario
        replay(mockedDatagramSocket);

        udpServerGameFinder = new UdpServerGameFinder(mockedDatagramSocket, BROADCAST_TIMEOUT_MS, udpBroadcastDataDecoder);
        udpServerGameFinder.setMaxAttemptsNumber(0);
        udpServerGameFinder.broadcastAndGetServerAddress();
        logback.info(udpServerGameFinder.toString());

        verify(mockedDatagramSocket); // Check that 2 tries where performed
        assertFalse(udpBroadcastDataDecoder.hasReceivedCorrectData());
        assertNull(udpBroadcastDataDecoder.getServerGameIp());
        assertNull(udpBroadcastDataDecoder.getPushAddress());
        assertNull(udpBroadcastDataDecoder.getSubscribeAddress());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
