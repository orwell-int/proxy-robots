package orwell.proxy.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.util.HashMap;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.zeromq.ZMQ;

import orwell.proxy.Camera;
import orwell.proxy.IRobot;
import orwell.proxy.IRobot.EnumConnectionState;
import orwell.proxy.ProxyRobots;
import orwell.proxy.Tank;

/**
 * Tests for {@link ProxyRobots}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ProxyRobotsTest extends EasyMockSupport {
	
	@Mock
	private Tank mockedTank;
	
	@TestSubject
	private ProxyRobots proxyRobots;
	
	@Before
	public void setUp() {
		proxyRobots = new ProxyRobots("orwell/proxy/test/configurationTest.xml", "localhost");
		mockedTank = createNiceMock(Tank.class);
	}
	
	@Test
	public void testInitialiseTanks() {
		proxyRobots.connectToServer();
		HashMap<String,Tank> tanksInitializedMap = new HashMap<String,Tank>();
		tanksInitializedMap.put("NicolasCage", mockedTank);
		proxyRobots.initialiseTanks(tanksInitializedMap);
		
		assertEquals(1, proxyRobots.getTanksInitializedMap().size());
		assertEquals(mockedTank, proxyRobots.getTanksInitializedMap().get("NicolasCage"));
	}
	
	@Test
	public void testConnectToRobots() {
		expect(mockedTank.connectToRobot()).andStubReturn(EnumConnectionState.CONNECTED);
		replay(mockedTank);
		
		proxyRobots.connectToRobots(proxyRobots.getTanksInitializedMap());
		
		//assertEquals(1, proxyRobots.getTanksConnectedMap().size());
	}
	
//	@Test
//	public void testRegister() {
//		ZMQ.Socket mockedZmqSocket = createMock(ZMQ.Socket.class);
//
//		//TODO
//		expect(mockedZmqSocket.recv()).andReturn(null);
//	}
	
}
