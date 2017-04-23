package orwell.proxy.zmq;

import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import orwell.proxy.EnumMessageType;

import java.util.ArrayList;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Tests for {@link ServerGameMessageBroker}.
 * <p/>
 * Created by Michael Ludmann on 15/03/15.
 */

@SuppressWarnings("unused")
@RunWith(PowerMockRunner.class)
@PrepareForTest(ZMQ.Socket.class)
public class ServerGameMessageBrokerTest {

    static final String TEST_ROUTING_ID_1 = "testRoutingId_1";
    static final String TEST_ROUTING_ID_2 = "testRoutingId_2";
    static final long OUTGOING_MSG_PERIOD_HIGH = 50000;
    private final static Logger logback = LoggerFactory.getLogger(ServerGameMessageBrokerTest.class);
    private final static long MAX_TIMEOUT_MS = 500;
    private final static String PUSH_ADDRESS = "tcp://127.0.0.1:9000";
    private final static String SUB_ADDRESS = "tcp://127.0.0.1:9001";
    private static final long WAIT_TIMEOUT_MS = 500;
    public static final int RECEIVE_TIMEOUT_MS = 100000;
    public static final int SENDER_LINGER = 1000;
    public static final int RECEIVER_LINGER = 1000;
    public static final int OUTGOING_MESSAGE_PERIOD = 42;
    private final FrequencyFilter frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

    @TestSubject
    private ServerGameMessageBroker zmf;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        final ArrayList<IFilter> filters = new ArrayList<>();
        filters.add(frequencyFilter);
        zmf = new ServerGameMessageBroker(RECEIVE_TIMEOUT_MS, SENDER_LINGER, RECEIVER_LINGER, OUTGOING_MESSAGE_PERIOD);
    }

    public void initZmqMocks() {
        // Mock ZMQ behaviour with mock sockets and context
        final ZMQ.Socket mockedZmqSocketSend = createNiceMock(ZMQ.Socket.class);
        final ZMQ.Socket mockedZmqSocketRecv = createNiceMock(ZMQ.Socket.class);
        final ZMQ.Context mockedZmqContext = createNiceMock(ZMQ.Context.class);

        expect(mockedZmqSocketSend.send((byte[]) anyObject(), anyInt())).andStubReturn(true);
        mockedZmqSocketSend.close();
        expectLastCall().once();
        replay(mockedZmqSocketSend);

        final byte[] raw_zmq_message = "routingIdTest Registered messageTest".getBytes();
        expect(mockedZmqSocketRecv.recv(ZMQ.NOBLOCK)).andStubReturn(raw_zmq_message);
        replay(mockedZmqSocketRecv);

        expect(mockedZmqContext.socket(ZMQ.PUSH)).andReturn(mockedZmqSocketSend);
        expect(mockedZmqContext.socket(ZMQ.SUB)).andReturn(mockedZmqSocketRecv);
        replay(mockedZmqContext);

        try {
            MemberModifier.field(ServerGameMessageBroker.class, "context").set(zmf, mockedZmqContext);
            MemberModifier.field(ServerGameMessageBroker.class, "sender").set(zmf, mockedZmqSocketSend);
            MemberModifier.field(ServerGameMessageBroker.class, "receiver").set(zmf, mockedZmqSocketRecv);
        } catch (final IllegalAccessException e) {
            logback.error(e.getMessage());
        }
    }

    // Wait for a max timeout or for communicationService to stop
    private void waitForCloseOrTimeout(final long timeoutMs) {
        long timeout = 0;

        while (zmf.isConnectedToServer() && timeoutMs > timeout) {
            try {
                Thread.sleep(5);
                timeout += 5;
            } catch (final InterruptedException e) {
                logback.error(e.getMessage());
            }
        }
    }

    @Test
    public void testConnect() {
        initZmqMocks();
        assertTrue(zmf.connectToServer(PUSH_ADDRESS, SUB_ADDRESS));
    }


    @Test
    public void testSendZmqMessage() {
        initZmqMocks();

        final byte[] msgBody = "msgBody".getBytes();

        final ZmqMessageBOM registerMsg = new ZmqMessageBOM(TEST_ROUTING_ID_1, EnumMessageType.REGISTER, msgBody);
        assertTrue(zmf.sendZmqMessage(registerMsg));

        final ZmqMessageBOM serverRobotStateMsg = new ZmqMessageBOM(TEST_ROUTING_ID_1, EnumMessageType.SERVER_ROBOT_STATE, msgBody);
        assertTrue(zmf.sendZmqMessage(serverRobotStateMsg));

        final ZmqMessageBOM registerEmptyBodyMsg = new ZmqMessageBOM(TEST_ROUTING_ID_1, EnumMessageType.REGISTER, new byte[0]);
        assertFalse("Zmq message should be empty and not sent",
                zmf.sendZmqMessage(registerEmptyBodyMsg));
    }

    @Test
    public void testClose() {
        initZmqMocks();

        zmf.close();
        assertFalse(zmf.isConnectedToServer());
    }

    @Test
    public void testSetSkipIncomingIdenticalMessages() {
        initZmqMocks();

        zmf.connectToServer(PUSH_ADDRESS, SUB_ADDRESS);
        zmf.setSkipIncomingIdenticalMessages(true);

        long timeout = 0;
        while (1 > zmf.getNbSuccessiveMessagesSkipped() && MAX_TIMEOUT_MS > timeout) {
            try {
                Thread.sleep(5);
                timeout += 5;
            } catch (final InterruptedException e) {
                logback.error(e.getMessage());
            }
        }

        assert (0 < zmf.getNbSuccessiveMessagesSkipped());
    }

    @Test
    public void testReceiveBadMessage() {
        // Mock ZMQ behaviour with mock sockets and context
        final ZMQ.Socket mockedZmqSocketSend = createNiceMock(ZMQ.Socket.class);
        final ZMQ.Socket mockedZmqSocketRecv = createNiceMock(ZMQ.Socket.class);
        final ZMQ.Context mockedZmqContext = createNiceMock(ZMQ.Context.class);

        expect(mockedZmqSocketSend.send((byte[]) anyObject(), anyInt())).andStubReturn(true);
        mockedZmqSocketSend.close();
        expectLastCall().once();
        replay(mockedZmqSocketSend);

        final byte[] raw_zmq_message_bad = "routingIdTest Registered".getBytes();
        expect(mockedZmqSocketRecv.recv(ZMQ.NOBLOCK)).andStubReturn(raw_zmq_message_bad);
        replay(mockedZmqSocketRecv);

        expect(mockedZmqContext.socket(ZMQ.PUSH)).andReturn(mockedZmqSocketSend);
        expect(mockedZmqContext.socket(ZMQ.SUB)).andReturn(mockedZmqSocketRecv);
        replay(mockedZmqContext);

        try {
            MemberModifier.field(ServerGameMessageBroker.class, "context").set(zmf, mockedZmqContext);
            MemberModifier.field(ServerGameMessageBroker.class, "sender").set(zmf, mockedZmqSocketSend);
            MemberModifier.field(ServerGameMessageBroker.class, "receiver").set(zmf, mockedZmqSocketRecv);
        } catch (final IllegalAccessException e) {
            logback.error(e.getMessage());
        }

        zmf.connectToServer(PUSH_ADDRESS, SUB_ADDRESS);
        waitForCloseOrTimeout(WAIT_TIMEOUT_MS);
        assertTrue(0 < zmf.getNbBadMessages());
    }

    @After
    public void tearDown() {
        zmf.close();
        logback.debug("<<<< OUT");
    }
}
