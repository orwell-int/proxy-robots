package orwell.proxy.test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;

import static org.junit.Assert.*;

import java.util.HashMap;

import lejos.pc.comm.NXTInfo;

import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import orwell.proxy.Camera;
import orwell.proxy.MessageFramework;
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
	private MessageFramework mockedMf;
	private Camera mockedCamera;

	@TestSubject
	private ProxyRobots proxyRobots;

	@Before
	public void setUp() {
		proxyRobots = new ProxyRobots(
				"orwell/proxy/test/configurationTest.xml", "localhost");

		mockedTank = createNiceMock(Tank.class);

		mockedMf = createNiceMock(MessageFramework.class);
		expect(mockedMf.ConnectToNXT(anyObject(NXTInfo.class))).andStubReturn(
				true);
		replay(mockedMf);

		mockedCamera = createNiceMock(Camera.class);
		expect(mockedCamera.getURL()).andStubReturn("http://NICOLAS.CAGE");
		replay(mockedCamera);
	}

	@Test
	public void testInitialiseTanks() {
		proxyRobots.connectToServer();
		HashMap<String, Tank> tanksInitializedMap = new HashMap<String, Tank>();
		tanksInitializedMap.put("NicolasCage", mockedTank);
		proxyRobots.initialiseTanks(tanksInitializedMap);

		assertEquals(1, proxyRobots.getTanksInitializedMap().size());
		assertEquals(mockedTank,
				proxyRobots.getTanksInitializedMap().get("NicolasCage"));
	}

	@Test
	public void testConnectToRobots() {
		proxyRobots.connectToServer();
		Tank myTank = new Tank("Btname", "BtId", mockedCamera, mockedMf);

		HashMap<String, Tank> tanksInitializedMap = new HashMap<String, Tank>();
		tanksInitializedMap.put("NicolasCage", myTank);
		proxyRobots.initialiseTanks(tanksInitializedMap);

		proxyRobots.connectToRobots(proxyRobots.getTanksInitializedMap());

		assertEquals(1, proxyRobots.getTanksConnectedMap().size());
	}

	// @Test
	// public void testRegister() {
	// ZMQ.Socket mockedZmqSocket = createMock(ZMQ.Socket.class);
	//
	// //TODO
	// expect(mockedZmqSocket.recv()).andReturn(null);
	// }

}
