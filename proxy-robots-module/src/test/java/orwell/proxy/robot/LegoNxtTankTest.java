package orwell.proxy.robot;

import lejos.mf.common.StreamUnitMessage;
import lejos.mf.common.UnitMessageType;
import lejos.mf.pc.MessageFramework;
import lejos.pc.comm.NXTInfo;
import org.easymock.Capture;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.ProtobufTest;
import orwell.proxy.mock.MockedCamera;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class LegoNxtTankTest {
    private final static Logger logback = LoggerFactory.getLogger(LegoNxtTankTest.class);
    private final static String COLOUR_VALUE = "2";
    private final static String INPUT_MOVE = "move 50.5 10.0";
    private final Capture<StreamUnitMessage> messageCapture = new Capture<>();
    private final StreamUnitMessage streamUnitMessageColour = new StreamUnitMessage(UnitMessageType.Colour, COLOUR_VALUE);
    private final StreamUnitMessage streamUnitMessageMove = new StreamUnitMessage(UnitMessageType.Command, INPUT_MOVE);

    @TestSubject
    private LegoNxtTank tank;

    @Mock
    private MessageFramework messageFramework;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        // Instantiate default tank
        tank = new LegoNxtTank("", "", new MockedCamera(), "");
    }

    private LegoNxtTank legoTankFromMF(final MessageFramework messageFramework) {
        return new LegoNxtTank("BTNameTest", "BT-IDTest", messageFramework, new MockedCamera(), "ImageTest");
    }

    @Test
    public void testToString() {
        final String tankString = "LegoNxtTank { [BTName]  [BT-ID]  [RoutingID] " + tank.getRoutingId() + " [TeamName]  }";
        assertEquals(tankString, tank.toString());
    }

    @Test
    public void testSendUnitMessage() {
        // Setup the LEGO BT message framework mock and instantiate the tank
        messageFramework = createNiceMock(MessageFramework.class);
        messageFramework.SendMessage(capture(messageCapture));
        expectLastCall().once();
        replay(messageFramework);

        tank = legoTankFromMF(messageFramework);

        try {
            tank.sendUnitMessage(streamUnitMessageMove);
        } catch (MessageNotSentException e) {
            logback.error(e.getMessage());
        }
        verify(messageFramework);
        assertEquals(UnitMessageType.Command, messageCapture.getValue().getMessageType());
        assertEquals(INPUT_MOVE, messageCapture.getValue().getPayload());
    }

    @Test
    public void testConnect_succeeds() {
        // Setup the LEGO BT message framework mock and instantiate the tank
        messageFramework = createNiceMock(MessageFramework.class);
        expect(messageFramework.ConnectToNXT(anyObject(NXTInfo.class))).andReturn(true);
        expectLastCall().once();
        replay(messageFramework);

        tank = legoTankFromMF(messageFramework);

        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());
        tank.connect();
        assertEquals(EnumConnectionState.CONNECTED, tank.getConnectionState());
        verify(messageFramework);
    }

    @Test
    public void testConnect_fails() {
        // Setup the LEGO BT message framework mock and instantiate the tank
        messageFramework = createNiceMock(MessageFramework.class);
        expect(messageFramework.ConnectToNXT(anyObject(NXTInfo.class))).andReturn(false);
        expectLastCall().once();
        replay(messageFramework);

        tank = legoTankFromMF(messageFramework);

        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());
        tank.connect();
        assertEquals(EnumConnectionState.CONNECTION_FAILED, tank.getConnectionState());
        verify(messageFramework);
    }


    @Test
    public void testCloseConnection() {
        // Setup the LEGO BT message framework mock and instantiate the tank
        messageFramework = createNiceMock(MessageFramework.class);
        messageFramework.close();
        expectLastCall().once();
        replay(messageFramework);

        tank = legoTankFromMF(messageFramework);

        // Set the tank in CONNECTED state
        tank.setConnectionState(EnumConnectionState.CONNECTED);

        tank.closeConnection();
        // Check that the LEGO BT message framework close() method was called
        verify(messageFramework);
        assertEquals(EnumConnectionState.NOT_CONNECTED, tank.getConnectionState());
    }

    @Test
    /**
     * 1. Tank reads an Colour value: first read of ServerRobotState is not null
     * 2. Nothing happens: second read of ServerRobotState is null
     * 3. Tank reads a Colour value: third read of ServerRobotState is not null
     */
    public void testReceivedNewMessage() {
        tank.receivedNewMessage(streamUnitMessageColour);

        final RobotElementStateVisitor stateVisitor = new RobotElementStateVisitor();
        tank.accept(stateVisitor);
        assertNotNull(stateVisitor.getServerRobotStateBytes());

        stateVisitor.clearServerRobotState();
        tank.accept(stateVisitor);
        logback.debug(stateVisitor.toString());
        assertNull(stateVisitor.getServerRobotStateBytes());

        tank.receivedNewMessage(streamUnitMessageColour);
        stateVisitor.clearServerRobotState();
        tank.accept(stateVisitor);
        assertNotNull(stateVisitor.getServerRobotStateBytes());
    }

    @Test
    public void testAccept_inputVisitor_mock() throws MessageNotSentException {
        // Simple test with mock visitor
        final RobotInputSetVisitor inputVisitor = createNiceMock(RobotInputSetVisitor.class);

        // Preparation of the mock input visitor
        // We check that we visit all sensors and the tank itself
        inputVisitor.visit((InputFire) anyObject());
        expectLastCall().once();
        inputVisitor.visit((InputMove) anyObject());
        expectLastCall().once();
        inputVisitor.visit(tank);
        expectLastCall().once();
        replay(inputVisitor);

        // Real call
        tank.accept(inputVisitor);

        // Check
        verify(inputVisitor);
    }

    @Test
    public void testAccept_inputVisitor_concrete() throws MessageNotSentException {
        // Setup the LEGO BT message framework mock and instantiate the tank
        messageFramework = createNiceMock(MessageFramework.class);
        messageFramework.SendMessage(capture(messageCapture));
        expectLastCall().once();
        replay(messageFramework);

        tank = legoTankFromMF(messageFramework);

        // Check with a concrete visitor
        // this is more of an integration test than a unit test
        final RobotInputSetVisitor inputSetVisitor = new RobotInputSetVisitor(ProtobufTest.getTestInput().toByteArray());
        tank.accept(inputSetVisitor);
        verify(messageFramework);
        assertEquals(UnitMessageType.Command, messageCapture.getValue().getMessageType());
        assertEquals(INPUT_MOVE, messageCapture.getValue().getPayload());
    }

    @Test
    public void testAccept_elementVisitor() {
        // Simple test with mock visitor
        final RobotElementStateVisitor stateVisitor = createNiceMock(RobotElementStateVisitor.class);

        // Preparation of the mock state visitor
        // We check that we visit all sensors and the tank itself
        stateVisitor.visit((ColourSensor) anyObject());
        expectLastCall().once();
        stateVisitor.visit((MockedCamera) anyObject());
        expectLastCall().once();
        stateVisitor.visit(tank);
        expectLastCall().once();
        replay(stateVisitor);

        // Real call
        tank.accept(stateVisitor);

        // Check
        verify(stateVisitor);
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
