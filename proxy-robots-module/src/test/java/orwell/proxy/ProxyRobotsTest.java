package orwell.proxy;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import orwell.messages.Robot;
import orwell.messages.ServerGame;
import orwell.messages.Controller;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.mock.MockedTank;
import orwell.proxy.mock.MockedZmqMessageFramework;


/**
 * Tests for {@link ProxyRobots}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */


public class ProxyRobotsTest {

    final static Logger logback = LoggerFactory.getLogger(ProxyRobotsTest.class);
    final static long MAX_TIMEOUT_MS = 500;

    @TestSubject
    private ProxyRobots myProxyRobots;

    @Mock
    //	private Tank myTank;
    private MockedTank mockedTank;
    private TankDeltaState mockedTankDeltaState;
    //private MockedZmqMessageFramework mockedZmqMessageFramework = new MockedZmqMessageFramework();
    private MockedZmqMessageFramework mockedZmqMessageFramework = createNiceMock(MockedZmqMessageFramework.class);


    @Before
    public void setUp() throws Exception { //expectPrivate might throw exceptions
        logback.info("IN");

        // Mock TankCurrentState
        final String modifyTankCurrentStateTimeStamp = "getTimeStamp";
        mockedTankDeltaState = createMockBuilder(TankDeltaState.class).withConstructor().addMockedMethod(modifyTankCurrentStateTimeStamp).createMock();
        expect(mockedTankDeltaState.getTimeStamp()).andStubReturn(new Long(999999999));
        replay(mockedTankDeltaState);

        ConfigFactory configFactory = new ConfigFactory("/configurationTest.xml", "localhost");

        RobotsMap robotsMap = new RobotsMap();
        mockedTank = new MockedTank();
        robotsMap.add(mockedTank);


        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework,
                configFactory.getConfigServerGame(),
                configFactory.getConfigRobots(),
                robotsMap);

        logback.info("OUT");
    }

    public byte[] getMockRawZmqMessage(IRobot iRobot, EnumMessageType messageType) {
        byte[] raw_zmq_message;
        byte[] specificMessage = new byte[0];
        String zmqMessageHeader = null;

        switch (messageType) {
            case REGISTERED:
                specificMessage = getBytesRegistered();
                zmqMessageHeader = iRobot.getRoutingID() + " " + "Registered" + " ";
                break;
            case SERVER_ROBOT_STATE:
                specificMessage = getBytesServerRobotState();
                zmqMessageHeader = iRobot.getRoutingID() + " " + "ServerRobotState" + " ";
                break;
            case INPUT:
                specificMessage = getBytesInput();
                zmqMessageHeader = iRobot.getRoutingID() + " " + "Input" + " ";
                break;
            case STOP:
                specificMessage = getBytesStop();
                zmqMessageHeader = iRobot.getRoutingID() + " " + "Stop" + " ";
                break;
            default:
                logback.error("Case : Message type " + messageType + " not handled");
        }

        raw_zmq_message = orwell.proxy.Utils.Concatenate(zmqMessageHeader.getBytes(),
                specificMessage);

        return raw_zmq_message;
    }

    public byte[] getBytesRegistered() {
        ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId("BananaOne");
        registeredBuilder.setTeam("BLUE");

        return registeredBuilder.build().toByteArray();
    }

    public byte[] getBytesInput() {
        Controller.Input.Builder inputBuilder = Controller.Input.newBuilder();
        Controller.Input.Fire.Builder fireBuilder = Controller.Input.Fire.newBuilder();
        Controller.Input.Move.Builder moveBuilder = Controller.Input.Move.newBuilder();
        fireBuilder.setWeapon1(false);
        fireBuilder.setWeapon2(false);
        moveBuilder.setLeft(0);
        moveBuilder.setRight(0);
        inputBuilder.setFire(fireBuilder.build());
        inputBuilder.setMove(moveBuilder.build());

        return inputBuilder.build().toByteArray();
    }

    public byte[] getBytesStop(){
        return "STOP".getBytes();
    }

    public byte[] getBytesServerRobotState()
    {
        Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
        Robot.Rfid.Builder rfidBuilder = Robot.Rfid.newBuilder();
        rfidBuilder.setRfid("1234");
        rfidBuilder.setStatus(Robot.Status.ON);
        rfidBuilder.setTimestamp(1234567890);
        serverRobotStateBuilder.addRfid(rfidBuilder.build());

        return serverRobotStateBuilder.build().toByteArray();
    }
	
	@Test
	public void testInitialiseTanks() {
		logback.info("IN");

		assertEquals(1, myProxyRobots.robotsMap.getRobotsArray().size());
		assertEquals(mockedTank,
				myProxyRobots.robotsMap.get("tempRoutingId"));
		logback.info("OUT");
	}

	@Test
	public void testConnectToRobots() {
		logback.info("IN");
		myProxyRobots.connectToRobots();

		assertEquals(1, myProxyRobots.robotsMap.getConnectedRobots().size());
		logback.info("OUT");
	}

    @Test
	public void testRegisterFlow() {
		logback.info("IN");

		myProxyRobots.connectToRobots();
		assertEquals(IRobot.EnumRegistrationState.NOT_REGISTERED, mockedTank.getRegistrationState());
        assertEquals("tempRoutingId", mockedTank.getRoutingID());

		myProxyRobots.sendRegister();
		myProxyRobots.startCommunicationService();
        myProxyRobots.receivedNewZmq(new ZmqMessageWrapper(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

		assertEquals(IRobot.EnumRegistrationState.REGISTERED, mockedTank.getRegistrationState());
        assertEquals("BananaOne", mockedTank.getRoutingID());

        logback.info("OUT");
	}


	@Test
	public void testUpdateConnectedTanks() {
		logback.info("IN");

		myProxyRobots.connectToRobots();
		assert(myProxyRobots.robotsMap.isRobotConnected("tempRoutingId"));
        myProxyRobots.sendRegister();

		myProxyRobots.startCommunicationService();

		// Tank is disconnected
        mockedTank.closeConnection();

        long timeout = 0;
        while(!myProxyRobots.robotsMap.getConnectedRobots().isEmpty() && timeout < MAX_TIMEOUT_MS)
        {
            try {
                Thread.sleep(5);
                timeout+= 5;
            } catch (InterruptedException e) {
                logback.error(e.getStackTrace().toString());
            }
        }
		// So the map of connected tanks is empty
		assert(myProxyRobots.robotsMap.getConnectedRobots().isEmpty());

        logback.debug("OUT");
	}

    @Test
    public void testSendServerRobotState() {
        logback.info("IN");

        expect(mockedZmqMessageFramework.sendZmqMessage((EnumMessageType) anyObject(),
                anyString(),
                (byte[])anyObject())).andReturn(true).once();
        replay(mockedZmqMessageFramework);

        myProxyRobots.sendServerRobotStates();

        //TODO fix this check
        //verify(mockedZmqMessageFramework);
        logback.info("OUT");
    }

    @After
    public void tearDown(){
        myProxyRobots.closeCommunicationService();
    }
}
