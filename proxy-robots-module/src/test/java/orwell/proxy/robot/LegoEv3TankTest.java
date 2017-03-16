package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.zmq.RobotMessageBroker;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Michaël Ludmann on 03/07/16.
 */
public class LegoEv3TankTest {
    private final static Logger logback = LoggerFactory.getLogger(LegoEv3TankTest.class);

    private static final String RFID_VALUE = "12345678";
    private final static String INPUT_MOVE = "move 50.5 10.0";
    private final String IP_ADDRESS = "192.168.0.17";
    private final int VIDEO_STREAM_PORT = 1111;
    private final String IMAGE = "YELLOW HULL";
    private final int PUSH_PORT = 10000;
    private final int PULL_PORT = 10001;
    private final String HOSTNAME = "HOSTNAME_TEST";
    private final Capture<UnitMessage> messageCapture = new Capture<>();
    private final UnitMessage unitMessageMove = new UnitMessage(UnitMessageType.Command, INPUT_MOVE);
    private LegoEv3Tank tank;

    @Before
    public void setUp() {
        // Instantiate default tank
        tank = new LegoEv3Tank(IP_ADDRESS,
                VIDEO_STREAM_PORT, IMAGE,
                PUSH_PORT, PULL_PORT, HOSTNAME);
    }

    private LegoEv3Tank legoEv3TankFromMB(final RobotMessageBroker messageBroker) {
        return new LegoEv3Tank(IP_ADDRESS,
                VIDEO_STREAM_PORT, IMAGE,
                HOSTNAME, messageBroker);
    }

    @Test
    public void testGetCamera() {
        assertEquals("nc:" + IP_ADDRESS + ":" + VIDEO_STREAM_PORT, tank.getCameraUrl());
    }

    @Test
    public void testSendUnitMessage() {
        tank.setRfidValue(RFID_VALUE);
        RobotMessageBroker messageBroker = createNiceMock(RobotMessageBroker.class);
        expect(messageBroker.send(capture(messageCapture))).andReturn(true);
        replay(messageBroker);

        tank = legoEv3TankFromMB(messageBroker);

        try {
            tank.sendUnitMessage(unitMessageMove);
        } catch (MessageNotSentException e) {
            logback.error(e.getMessage());
        }

        verify(messageBroker);
        assertEquals(UnitMessageType.Command, messageCapture.getValue().getMessageType());
        assertEquals(INPUT_MOVE, messageCapture.getValue().getPayload());
    }

    @Test
    public void testConnect_succeeds() {
        RobotMessageBroker messageBroker = createNiceMock(RobotMessageBroker.class);
        messageBroker.bind();
        expectLastCall().once();
        replay(messageBroker);

        tank = legoEv3TankFromMB(messageBroker);

        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());
        tank.connect(); // Trying to connect, this does not mean it is connected right away
        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());

        // We need to wait for the messageBroker to tell the robot it received a Connection message
        tank.receivedNewMessage(new UnitMessage(UnitMessageType.Connection, "connected"));
        assertEquals(EnumConnectionState.CONNECTED, tank.getConnectionState());
    }

    @Test
    public void testCloseConnection() {
        RobotMessageBroker messageBroker = createNiceMock(RobotMessageBroker.class);
        messageBroker.close();
        expectLastCall().once();
        replay(messageBroker);

        tank = legoEv3TankFromMB(messageBroker);

        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());
        tank.connect(); // Trying to connect, this does not mean it is connected right away
        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());

        // We need to wait for the messageBroker to tell the robot it received a Connection message
        tank.receivedNewMessage(new UnitMessage(UnitMessageType.Connection, "connected"));
        assertEquals(EnumConnectionState.CONNECTED, tank.getConnectionState());

        tank.closeConnection();
        // Check that the message broker close() method was called
        verify(messageBroker);
        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());
    }


}