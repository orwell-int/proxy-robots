package orwell.proxy;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;

import java.util.HashMap;

import lejos.pc.comm.NXTInfo;

import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.runner.RunWith;
import org.zeromq.ZMQ;

import orwell.messages.ServerGame;
import orwell.proxy.Camera;
import orwell.proxy.IRobot;
import orwell.proxy.MessageFramework;
import orwell.proxy.ProxyRobots;
import orwell.proxy.Tank;
import orwell.proxy.ZmqMessageWrapper;

/**
 * Tests for {@link ProxyRobots}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ZMQ.Socket.class })
public class ProxyRobotsTest {
	
	final static Logger logback = LoggerFactory.getLogger(ProxyRobotsTest.class); 
	private enum EnumMessageType {
		REGISTER,
		REGISTERED,
		INPUT;
	}
	
	@TestSubject
	private ProxyRobots proxyRobots;
	
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
		
		// TODO debug this part
		mockedZmqSocketSend.setLinger(1000);
		expectLastCall().times(1);
		
		logback.info("TEST getZMQRegisterHeader: " + myTank.getZMQRegister());

		expect(mockedZmqSocketSend.send(myTank.getZMQRegister(), 0)).andReturn(true);
		logback.info("BATMAN");
		//		expectLastCall().times(1);
		replay(mockedZmqSocketSend);
		
		expect(mockedZmqSocketRecv.recv()).andStubReturn(getMockRawZmqMessage(myTank, EnumMessageType.REGISTERED));
		replay(mockedZmqSocketRecv);
		
		expect(mockedZmqContext.socket(ZMQ.PUSH)).andStubReturn(mockedZmqSocketSend);
		expect(mockedZmqContext.socket(ZMQ.SUB)).andStubReturn(mockedZmqSocketRecv);
		replay(mockedZmqContext);

		proxyRobots = new ProxyRobots(
				"orwell/proxy/test/configurationTest.xml", "localhost", mockedZmqContext);
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

	public void createAndInitializeTank(ProxyRobots proxyrobots)
	{
		logback.info("IN");
		proxyRobots.connectToServer();

		HashMap<String, Tank> tanksInitializedMap = new HashMap<String, Tank>();
		tanksInitializedMap.put("NicolasCage", myTank);
		proxyRobots.initialiseTanks(tanksInitializedMap);
		logback.info("OUT");
	}
	
	@Test
	public void testInitialiseTanks() {
		logback.info("IN");
		createAndInitializeTank(this.proxyRobots);

		assertEquals(1, proxyRobots.getTanksInitializedMap().size());
		assertEquals(myTank,
				proxyRobots.getTanksInitializedMap().get("NicolasCage"));
		logback.info("OUT");
	}

	@Test
	public void testConnectToRobots() {
		logback.info("IN");
		createAndInitializeTank(this.proxyRobots);
		proxyRobots.connectToRobots();

		assertEquals(1, proxyRobots.getTanksConnectedMap().size());
		logback.info("OUT");
	}

	@Test
	public void testRegister() {
		logback.info("IN");

		createAndInitializeTank(proxyRobots);

		proxyRobots.connectToRobots();
		assertEquals(IRobot.EnumRegistrationState.NOT_REGISTERED, myTank.getRegistrationState());

		proxyRobots.registerRobots();
		proxyRobots.startCommunication(new ZmqMessageWrapper(getMockRawZmqMessage(myTank, EnumMessageType.REGISTERED)));
		
		assertEquals(IRobot.EnumRegistrationState.REGISTERED, myTank.getRegistrationState());

		logback.info("OUT");
	}
	
	public void tearDown(){
	}

}
