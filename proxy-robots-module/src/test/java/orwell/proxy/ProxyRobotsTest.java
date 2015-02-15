package orwell.proxy;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createNiceMock;

import java.util.HashMap;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import lejos.pc.comm.NXTInfo;

import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import orwell.messages.Robot;
import orwell.messages.ServerGame;
import orwell.messages.Controller;
import lejos.mf.pc.MessageFramework;


/**
 * Tests for {@link ProxyRobots}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ZMQ.Socket.class } )
public class ProxyRobotsTest {
	
	final static Logger logback = LoggerFactory.getLogger(ProxyRobotsTest.class); 
	private enum EnumMessageType {
		REGISTER,
		REGISTERED,
        SERVERROBOTSTATE,
		INPUT;
	}
	
	@TestSubject
	private ProxyRobots myProxyRobots;

	@Mock
	private ZMQ.Socket mockedZmqSocketSend;
	private ZMQ.Socket mockedZmqSocketRecv;
	private MessageFramework mockedMf;
	private Camera mockedCamera;
	private ZMQ.Context mockedZmqContext;
	private Tank myTank;

	@Before
	public void setUp() {
		logback.info("IN");

        // Mock Message framework
		mockedMf = createNiceMock(MessageFramework.class);
		expect(mockedMf.ConnectToNXT(anyObject(NXTInfo.class))).andStubReturn(
				true);
		replay(mockedMf);

        // Mock camera
		mockedCamera = createNiceMock(Camera.class);
		expect(mockedCamera.getURL()).andStubReturn("192.168.1.50");
		replay(mockedCamera);

        // Mock one tank
		myTank = new Tank("Btname", "BtId", mockedCamera, mockedMf, "");
		myTank.setRoutingID("NicolasCage");

        // Mock ZMQ behavior with mock sockets and context
		mockedZmqSocketSend = createNiceMock(ZMQ.Socket.class);
		mockedZmqSocketRecv = createNiceMock(ZMQ.Socket.class);
		mockedZmqContext = createNiceMock(ZMQ.Context.class);

		mockedZmqSocketSend.setLinger(1000);
		expectLastCall().once();

		expect(mockedZmqSocketSend.send(myTank.getZmqRegister())).andStubReturn(true);
        expect(mockedZmqSocketSend.send(getMockRawZmqMessage(myTank, EnumMessageType.SERVERROBOTSTATE))).andStubReturn(true);
        mockedZmqSocketSend.close();
		expectLastCall().once();
		replay(mockedZmqSocketSend);
		
		expect(mockedZmqSocketRecv.recv(ZMQ.NOBLOCK)).andStubReturn(getMockRawZmqMessage(myTank, EnumMessageType.REGISTERED));
		mockedZmqSocketRecv.close();
		expectLastCall().once();
		replay(mockedZmqSocketRecv);

		expect(mockedZmqContext.socket(ZMQ.PUSH)).andReturn(mockedZmqSocketSend);
		expect(mockedZmqContext.socket(ZMQ.SUB)).andReturn(mockedZmqSocketRecv);
		replay(mockedZmqContext);

        // Instantiate main class with mock parameters
		myProxyRobots = new ProxyRobots(
				"/configurationTest.xml", "localhost", mockedZmqContext);

		logback.info("OUT");
	}
	
	public byte[] getMockRawZmqMessage(Tank tank, EnumMessageType messageType)
	{
		byte[] raw_zmq_message;
		byte[] specificMessage = new byte[0];
        String zmqMessageHeader = null;

		switch(messageType){
            case REGISTERED:
                specificMessage = getBytesRegistered();
                zmqMessageHeader = tank.getRoutingID() + " " + "Registered" + " ";
                break;
            case SERVERROBOTSTATE:
                specificMessage = getBytesServerRobotState();
                zmqMessageHeader = tank.getRoutingID() + " " + "ServerRobotState" + " ";
                break;
            case INPUT:
                specificMessage = getBytesInput();
                zmqMessageHeader = tank.getRoutingID() + " " + "Input" + " ";
                break;
		default:
			logback.error("Case : Message type " + messageType + " not handled");
		}

		raw_zmq_message = orwell.proxy.Utils.Concatenate(zmqMessageHeader.getBytes(),
				specificMessage);
		
		return raw_zmq_message;
	}
	
	public byte[] getBytesRegistered()
	{		
		ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
		registeredBuilder.setRobotId("BananaOne");
		registeredBuilder.setTeam(ServerGame.EnumTeam.BLU);
		
		return registeredBuilder.build().toByteArray();
	}

    public byte[] getBytesInput()
    {
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
		tanksInitializedMap.put("NicolasCage", myTank);
		iProxyRobots.initializeTanks(tanksInitializedMap);
		logback.info("OUT");
	}
	
	@Test
	public void testInitialiseTanks() {
		logback.info("IN");
		createAndInitializeTank(myProxyRobots);

		assertEquals(1, myProxyRobots.getTanksInitializedMap().size());
		assertEquals(myTank,
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
	public void testRegister() {
		logback.info("IN");

		createAndInitializeTank(myProxyRobots);

		myProxyRobots.connectToRobots();
		assertEquals(IRobot.EnumRegistrationState.NOT_REGISTERED, myTank.getRegistrationState());

		myProxyRobots.registerRobots();
		myProxyRobots.startCommunication(new ZmqMessageWrapper(getMockRawZmqMessage(myTank, EnumMessageType.REGISTERED)));

		assertEquals(IRobot.EnumRegistrationState.REGISTERED, myTank.getRegistrationState());

		logback.info("OUT");
	}

	@Test
	public void testUpdateConnectedTanks() {
		logback.info("IN");

		createAndInitializeTank(myProxyRobots);

		myProxyRobots.connectToRobots();
		myProxyRobots.registerRobots();
		assert(myProxyRobots.getTanksConnectedMap().containsKey("NicolasCage"));

		myProxyRobots.startCommunication(new ZmqMessageWrapper(getMockRawZmqMessage(myTank, EnumMessageType.REGISTERED)));

		// Tank is disconnected
		myTank.closeConnection();
		myProxyRobots.stopCommunication();

		// So the map of connected tanks is empty
		assert(myProxyRobots.getTanksConnectedMap().isEmpty());

		// And the communication is closed
		PowerMock.verify(mockedZmqSocketSend);
        PowerMock.verify(mockedZmqSocketRecv);

        logback.debug("OUT");
	}

//    @Test
//    public void testSendServerRobotState() {
//        logback.info("IN");
//        createAndInitializeTank(myProxyRobots);
//
//        myProxyRobots.connectToRobots();
//        myProxyRobots.registerRobots();
//
//        myProxyRobots.startCommunication(new ZmqMessageWrapper(getMockRawZmqMessage(myTank, EnumMessageType.SERVERROBOTSTATE)));
//
//        //TODO properly Mock myTank to mock timestamp
//        UnitMessage unitMessage = new UnitMessage(UnitMessageType.Rfid, "1234");
//        myTank.receivedNewMessage(unitMessage);
//
//        myTank.closeConnection();
//        myProxyRobots.stopCommunication();
//        PowerMock.verify(mockedZmqSocketSend);
//        PowerMock.verify(mockedZmqSocketRecv);
//
//        logback.info("OUT");
//    }
	
	public void tearDown(){
	}

}
