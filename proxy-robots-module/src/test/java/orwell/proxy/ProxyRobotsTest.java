package orwell.proxy;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createNiceMock;

import java.util.HashMap;

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

import orwell.messages.ServerGame;
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
		mockedMf = createNiceMock(MessageFramework.class);
		expect(mockedMf.ConnectToNXT(anyObject(NXTInfo.class))).andStubReturn(
				true);
		replay(mockedMf);

		mockedCamera = createNiceMock(Camera.class);
		expect(mockedCamera.getURL()).andStubReturn("192.168.1.50");
		replay(mockedCamera);
		
		myTank = new Tank("Btname", "BtId", mockedCamera, mockedMf, "");
		myTank.setRoutingID("NicolasCage");
		
		mockedZmqSocketSend = createNiceMock(ZMQ.Socket.class);
		mockedZmqSocketRecv = createNiceMock(ZMQ.Socket.class);
		mockedZmqContext = createNiceMock(ZMQ.Context.class);

		mockedZmqSocketSend.setLinger(1000);
		expectLastCall().once();

		expect(mockedZmqSocketSend.send(myTank.getZMQRegister())).andStubReturn(true);
		mockedZmqSocketSend.close();
		expectLastCall().once();
		replay(mockedZmqSocketSend);
		
		expect(mockedZmqSocketRecv.recv()).andStubReturn(getMockRawZmqMessage(myTank, EnumMessageType.REGISTERED));
		mockedZmqSocketRecv.close();
		expectLastCall().once();
		replay(mockedZmqSocketRecv);

		expect(mockedZmqContext.socket(ZMQ.PUSH)).andReturn(mockedZmqSocketSend);
		expect(mockedZmqContext.socket(ZMQ.SUB)).andReturn(mockedZmqSocketRecv);
		replay(mockedZmqContext);

		myProxyRobots = new ProxyRobots(
				"/configurationTest.xml", "localhost", mockedZmqContext);

		logback.info("OUT");
	}
	
	public byte[] getMockRawZmqMessage(Tank tank, EnumMessageType messageType)
	{
		byte[] raw_zmq_message;
		byte[] specificMessage = new byte[0];
		
		switch(messageType){
		case REGISTERED:
			specificMessage = getBytesRegistered();
			break;
		default:
			logback.error("Case : Message type " + messageType + " not handled");
		}
		
		String zMQmessageHeader = tank.getRoutingID() + " " + "Registered" + " ";
		raw_zmq_message = orwell.proxy.Utils.Concatenate(zMQmessageHeader.getBytes(),
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

		// So the map is empty
		assert(myProxyRobots.getTanksConnectedMap().isEmpty());

		// And the communication is closed TODO: make the check work
		PowerMock.verify(mockedZmqSocketSend);

		logback.debug("OUT");
	}
	
	public void tearDown(){
	}

}
