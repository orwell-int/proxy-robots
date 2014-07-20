package orwell.proxy.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;

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
	
	private Camera camera = new Camera("", 0);
	
	@Mock
//	private Tank mockedTank;
	
	@TestSubject
	private ProxyRobots proxyRobots = new ProxyRobots("orwell/proxy/test/configurationTest.xml", "localhost");
	
	@Before
	public void setUp() {
		System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOO");
		//proxyRobots = new ProxyRobots("orwell/proxy/test/configurationTest.xml", "localhost");
	}
	
	
	
	@Test
	public void testInitialiseTanks() {
		Tank mockedTank = createMock(Tank.class);
		expect(mockedTank.connectToRobot()).andStubReturn(EnumConnectionState.CONNECTED);
		replay(mockedTank);
		proxyRobots.connectToServer();
		HashMap<String,Tank> tanksInitializedMap = new HashMap<String,Tank>();
		tanksInitializedMap.put("NicolasCage", mockedTank);
		proxyRobots.initialiseTanks(tanksInitializedMap);
	}
	
//	@Test
//	public void testRegister() {
//		ZMQ.Socket mockedZmqSocket = createMock(ZMQ.Socket.class);
//
//		//TODO
//		expect(mockedZmqSocket.recv()).andReturn(null);
//	}
	
}
