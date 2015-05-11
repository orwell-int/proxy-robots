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
 * Tests for {@link ZmqMessageBroker}.
 * <p/>
 * Created by Michael Ludmann on 15/03/15.
 */

@SuppressWarnings("unused")
@RunWith(PowerMockRunner.class)
@PrepareForTest(ZMQ.Socket.class)
public class ZmqMessageBrokerTest {

    static final String TEST_ROUTING_ID_1 = "testRoutingId_1";
    static final String TEST_ROUTING_ID_2 = "testRoutingId_2";
    static final long OUTGOING_MSG_PERIOD_HIGH = 50000;
    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageBrokerTest.class);
    private final static long MAX_TIMEOUT_MS = 500;
    private final static int PUSH_PORT = 9000;
    private final static int SUB_PORT = 9001;
    private final FrequencyFilter frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

    @TestSubject
    private ZmqMessageBroker zmf;

    @Before
    public void setUp() {
        logback.info("IN");
        final ArrayList<IFilter> filters = new ArrayList<>();
        filters.add(frequencyFilter);
        zmf = new ZmqMessageBroker(1000, 1000, filters);

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
            MemberModifier.field(ZmqMessageBroker.class, "context").set(zmf, mockedZmqContext);
            MemberModifier.field(ZmqMessageBroker.class, "sender").set(zmf, mockedZmqSocketSend);
            MemberModifier.field(ZmqMessageBroker.class, "receiver").set(zmf, mockedZmqSocketRecv);
        } catch (final IllegalAccessException e) {
            logback.error(e.getMessage());
        }
        logback.info("OUT");
    }

    @Test
    public void testConnect() {
        logback.info("IN");
        assertTrue(zmf.connectToServer("127.0.0.1", PUSH_PORT, SUB_PORT));
        logback.info("OUT");
    }


    @Test
    public void testSendZmqMessage() {
        logback.info("IN");
        final byte[] msgBody = "msgBody".getBytes();

        final ZmqMessageBOM registerMsg = new ZmqMessageBOM(EnumMessageType.REGISTER, TEST_ROUTING_ID_1, msgBody);
        assertTrue(zmf.sendZmqMessage(registerMsg));

        final ZmqMessageBOM serverRobotStateMsg = new ZmqMessageBOM(EnumMessageType.SERVER_ROBOT_STATE, TEST_ROUTING_ID_1, msgBody);
        assertTrue(zmf.sendZmqMessage(serverRobotStateMsg));

        final ZmqMessageBOM registerEmptyBodyMsg = new ZmqMessageBOM(EnumMessageType.REGISTER, TEST_ROUTING_ID_1, new byte[0]);
        assertFalse("Zmq message should be empty and not sent",
                zmf.sendZmqMessage(registerEmptyBodyMsg));

        logback.info("OUT");
    }

    @Test
    public void testClose() {
        logback.info("IN");
        zmf.close();
        assertFalse(zmf.isConnectedToServer());
        logback.info("OUT");
    }

    @Test
    public void testSetSkipIncomingIdenticalMessages() {
        logback.info("IN");

        zmf.connectToServer("127.0.0.1", PUSH_PORT, SUB_PORT);
        zmf.setSkipIncomingIdenticalMessages(true);

        long timeout = 0;
        while (1 > zmf.getNbMessagesSkipped() && MAX_TIMEOUT_MS > timeout) {
            try {
                //noinspection BusyWait
                Thread.sleep(5);
                timeout += 5;
            } catch (final InterruptedException e) {
                logback.error(e.getMessage());
            }
        }

        assert (0 < zmf.getNbMessagesSkipped());

        logback.info("OUT");
    }

    @Test
    public void testSendZmqMessage_withFilter() throws Exception {
        logback.info("IN");
        final byte[] msgBody = "msgBody".getBytes();

        final ZmqMessageBOM registerMsg =
                new ZmqMessageBOM(EnumMessageType.REGISTER, TEST_ROUTING_ID_1, msgBody);
        assertTrue(zmf.sendZmqMessage(registerMsg));

        // Second identical message trying to be sent during the filtering period,
        // So the sending fails
        Thread.sleep(1);
        assertFalse(zmf.sendZmqMessage(registerMsg));

        // Third message is of a different type, so it is not filtered
        Thread.sleep(1);
        final ZmqMessageBOM serverRobotStateMsg =
                new ZmqMessageBOM(EnumMessageType.SERVER_ROBOT_STATE, TEST_ROUTING_ID_1, msgBody);
        assertTrue(zmf.sendZmqMessage(serverRobotStateMsg));

        // Fourth message is of a different routingId, so it is not filtered
        Thread.sleep(1);
        final ZmqMessageBOM serverRobotStateMsg_r2 =
                new ZmqMessageBOM(EnumMessageType.SERVER_ROBOT_STATE, TEST_ROUTING_ID_2, msgBody);
        assertTrue(zmf.sendZmqMessage(serverRobotStateMsg_r2));
        logback.info("OUT");
    }

    @After
    public void tearDown() {
        zmf.close();
    }
}
