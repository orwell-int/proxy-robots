package orwell.proxy;

import org.easymock.Mock;
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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Tests for {@link ZmqMessageFramework}.
 * <p/>
 * Created by parapampa on 15/03/15.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(ZMQ.Socket.class)
public class ZmqMessageFrameworkTest {

    final static Logger logback = LoggerFactory.getLogger(ZmqMessageFrameworkTest.class);
    final static long MAX_TIMEOUT_MS = 500;

    @TestSubject
    private ZmqMessageFramework zmf;

    @Mock
    private ZMQ.Socket mockedZmqSocketSend;
    private ZMQ.Context mockedZmqContext;

    @Before
    public void setUp() {
        logback.info("IN");
        zmf = new ZmqMessageFramework(1000, 1000, null);

        // Mock ZMQ behavior with mock sockets and context
        mockedZmqSocketSend = createNiceMock(ZMQ.Socket.class);
        final ZMQ.Socket mockedZmqSocketRecv = createNiceMock(ZMQ.Socket.class);
        mockedZmqContext = createNiceMock(ZMQ.Context.class);

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
            MemberModifier.field(ZmqMessageFramework.class, "context").set(zmf, mockedZmqContext);
            MemberModifier.field(ZmqMessageFramework.class, "sender").set(zmf, mockedZmqSocketSend);
            MemberModifier.field(ZmqMessageFramework.class, "receiver").set(zmf, mockedZmqSocketRecv);
        } catch (final IllegalAccessException e) {
            logback.error(e.getMessage());
        }
        logback.info("OUT");
    }

    @Test
    public void testConnect() {
        logback.info("IN");
        assertTrue(zmf.connectToServer("127.0.0.1", 9000, 9001));
        logback.info("OUT");
    }


    @Test
    public void testSendZmqMessage() {
        logback.info("IN");
        final byte[] msgBody = new String("msgBody").getBytes();

        final ZmqMessageBOM registerMsg = new ZmqMessageBOM(EnumMessageType.REGISTER, "BananaOne", msgBody);
        assertTrue(zmf.sendZmqMessage(registerMsg));

        final ZmqMessageBOM serverRobotStateMsg = new ZmqMessageBOM(EnumMessageType.SERVER_ROBOT_STATE, "BananaOne", msgBody);
        assertTrue(zmf.sendZmqMessage(serverRobotStateMsg));

        final ZmqMessageBOM registerEmptyBodyMsg = new ZmqMessageBOM(EnumMessageType.REGISTER, "BananaOne", new byte[0]);
        assertFalse("Zmq message should be empty and not sent",
                zmf.sendZmqMessage(registerEmptyBodyMsg));

        logback.info("OUT");
    }

    @Test
    public void testClose() {
        logback.info("IN");
        zmf.close();
        assertFalse(zmf.connected);
        logback.info("OUT");
    }

    @Test
    public void testSetSkipIdenticalMessages() {
        logback.info("IN");

        zmf.connectToServer("127.0.0.1", 9000, 9001);
        zmf.setSkipIdenticalMessages(true);

        long timeout = 0;
        while (1 > zmf.nbMessagesSkipped && MAX_TIMEOUT_MS > timeout) {
            try {
                Thread.sleep(5);
                timeout += 5;
            } catch (InterruptedException e) {
                logback.error(e.getStackTrace().toString());
            }
        }

        assert (0 < zmf.nbMessagesSkipped);

        logback.info("OUT");
    }

    //TODO new test for filtering step


    @After
    public void tearDown() {
        zmf.close();
    }
}
