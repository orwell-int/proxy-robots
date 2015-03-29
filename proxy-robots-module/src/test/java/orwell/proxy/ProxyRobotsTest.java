package orwell.proxy;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import orwell.messages.Robot;
import orwell.messages.ServerGame;
import orwell.messages.Controller;


/**
 * Tests for {@link ProxyRobots}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ZMQ.Socket.class, TankCurrentState.class, Tank.class } )
public class ProxyRobotsTest {

    final static Logger logback = LoggerFactory.getLogger(ProxyRobotsTest.class);

    @TestSubject
    private ProxyRobots myProxyRobots;

    @Mock
    private Camera mockedCamera;
    //	private Tank myTank;
    private Tank mockedTank;
    private TankCurrentState mockedTankCurrentState;

    @Before
    public void setUp() throws Exception { //expectPrivate might throw exceptions
        logback.info("IN");

        // Mock camera
        mockedCamera = createNiceMock(Camera.class);
        expect(mockedCamera.getURL()).andStubReturn("192.168.1.50");
        replay(mockedCamera);

        // Mock TankCurrentState
        final String modifyTankCurrentStateTimeStamp = "getTimeStamp";
        mockedTankCurrentState = createMockBuilder(TankCurrentState.class).withConstructor().addMockedMethod(modifyTankCurrentStateTimeStamp).createMock();
        expect(mockedTankCurrentState.getTimeStamp()).andStubReturn(new Long(999999999));
        replay(mockedTankCurrentState);

        // Mock one tank
        final String modifyTankCurrentState = "getTankCurrentState";
        final String modifyConnectionState = "getConnectionState";
        final String modifyTankConnect = "connectToRobot";
        mockedTank = createMockBuilder(Tank.class)
                .withConstructor("Btname", "BtId", mockedCamera, "")
                .addMockedMethods(modifyTankCurrentState, modifyTankConnect,
                                  modifyConnectionState)
                .createMock();
        mockedTank.setRoutingID("NicolasCage");
        expect(mockedTank.connectToRobot()).andStubReturn(IRobot.EnumConnectionState.CONNECTED);
        expect(mockedTank.getConnectionState()).andStubReturn(IRobot.EnumConnectionState.NOT_CONNECTED);
        expect(mockedTank.getConnectionState()).andReturn(IRobot.EnumConnectionState.CONNECTED).times(100);
        expect(mockedTank.getTankCurrentState()).andStubReturn(mockedTankCurrentState);
        replay(mockedTank);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(
                "/configurationTest.xml", "localhost");

        logback.info("OUT");
    }

    public byte[] getMockRawZmqMessage(Tank tank, EnumMessageType messageType) {
        byte[] raw_zmq_message;
        byte[] specificMessage = new byte[0];
        String zmqMessageHeader = null;

        switch (messageType) {
            case REGISTERED:
                specificMessage = getBytesRegistered();
                zmqMessageHeader = tank.getRoutingID() + " " + "Registered" + " ";
                break;
            case SERVER_ROBOT_STATE:
                specificMessage = getBytesServerRobotState();
                zmqMessageHeader = tank.getRoutingID() + " " + "ServerRobotState" + " ";
                break;
            case INPUT:
                specificMessage = getBytesInput();
                zmqMessageHeader = tank.getRoutingID() + " " + "Input" + " ";
                break;
            case STOP:
                specificMessage = getBytesStop();
                zmqMessageHeader = tank.getRoutingID() + " " + "Stop" + " ";
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
        registeredBuilder.setTeam(ServerGame.EnumTeam.BLU);

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

	public void createAndInitializeTank(ProxyRobots iProxyRobots)
	{
		logback.info("IN");
		iProxyRobots.connectToServer();

		HashMap<String, Tank> tanksInitializedMap = new HashMap<String, Tank>();
		tanksInitializedMap.put("NicolasCage", mockedTank);
		iProxyRobots.initializeTanks(tanksInitializedMap);
		logback.info("OUT");
	}
	
	@Test
	public void testInitialiseTanks() {
		logback.info("IN");
		createAndInitializeTank(myProxyRobots);

		assertEquals(1, myProxyRobots.getTanksInitializedMap().size());
		assertEquals(mockedTank,
				myProxyRobots.getTanksInitializedMap().get("NicolasCage"));
		logback.info("OUT");
	}

	@Test
	public void testConnectToRobots() {
		logback.info("IN");
		createAndInitializeTank(myProxyRobots);
		myProxyRobots.connectToRobots();

		assertEquals(1, myProxyRobots.getTanksConnectedMap().size());
		logback.info("OUT");
	}

    @Test
	public void testRegisterFlow() {
		logback.info("IN");

		createAndInitializeTank(myProxyRobots);

		myProxyRobots.connectToRobots();
		assertEquals(IRobot.EnumRegistrationState.NOT_REGISTERED, mockedTank.getRegistrationState());
        assertEquals("NicolasCage", mockedTank.getRoutingID());

		myProxyRobots.registerRobots();
		myProxyRobots.startCommunicationService();
        myProxyRobots.receivedNewZmq(new ZmqMessageWrapper(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

		assertEquals(IRobot.EnumRegistrationState.REGISTERED, mockedTank.getRegistrationState());
        assertEquals("BananaOne", mockedTank.getRoutingID());

        myProxyRobots.closeCommunicationService();
        // TODO make sure we fully process closeCommunicationService()

        logback.info("OUT");
	}

//	@Test
//	public void testRegister() {
//		logback.info("IN");
//
//		createAndInitializeTank(myProxyRobots);
//
//		myProxyRobots.connectToRobots();
//		assertEquals(IRobot.EnumRegistrationState.NOT_REGISTERED, mockedTank.getRegistrationState());
//        assertEquals("NicolasCage", mockedTank.getRoutingID());
//
//		myProxyRobots.registerRobots();
//		myProxyRobots.startCommunicationService(new ZmqMessageWrapper(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));
//
//		assertEquals(IRobot.EnumRegistrationState.REGISTERED, mockedTank.getRegistrationState());
//        assertEquals("BananaOne", mockedTank.getRoutingID());
//
//		logback.info("OUT");
//	}
//
//	@Test
//	public void testUpdateConnectedTanks() {
//		logback.info("IN");
//
//		createAndInitializeTank(myProxyRobots);
//
//		myProxyRobots.connectToRobots();
//		myProxyRobots.registerRobots();
//		assert(myProxyRobots.getTanksConnectedMap().containsKey("NicolasCage"));
//
//		myProxyRobots.startCommunicationService(new ZmqMessageWrapper(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));
//
//		// Tank is disconnected
//        mockedTank.closeConnection();
//		myProxyRobots.closeCommunicationService();
//
//		// So the map of connected tanks is empty
//		assert(myProxyRobots.getTanksConnectedMap().isEmpty());
//
//		// And the communication is closed
//		PowerMock.verify(mockedZmqSocketSend);
//        PowerMock.verify(mockedZmqSocketRecv);
//
//        logback.debug("OUT");
//	}

//    @Test
//    public void testSendServerRobotState() {
//        logback.info("IN");
//        createAndInitializeTank(myProxyRobots);
//
//        myProxyRobots.connectToRobots();
//        myProxyRobots.registerRobots();
//
//        myProxyRobots.startCommunicationService(new ZmqMessageWrapper(getMockRawZmqMessage(mockedTank, EnumMessageType.STOP)));
//
//        UnitMessage unitMessage = new UnitMessage(UnitMessageType.Rfid, "1234");
//        mockedTank.receivedNewMessage(unitMessage);
//
//        mockedTank.closeConnection();
//        myProxyRobots.closeCommunicationService();
//        PowerMock.verify(mockedZmqSocketSend);
//        PowerMock.verify(mockedZmqSocketRecv);
//
//        logback.info("OUT");
//    }

    @After
    public void tearDown(){
    }
}
