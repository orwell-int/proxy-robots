package orwell.proxy;

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
import orwell.messages.Controller;
import orwell.messages.ServerGame;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.ConfigFactoryParameters;
import orwell.proxy.config.EnumConfigFileType;
import orwell.proxy.mock.MockedTank;
import orwell.proxy.robot.EnumRegistrationState;
import orwell.proxy.robot.IRobot;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ZmqMessageBOM;
import orwell.proxy.zmq.ZmqMessageBroker;

import java.net.DatagramSocket;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


/**
 * Tests for {@link ProxyRobots}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */


@SuppressWarnings("unused")
@RunWith(JUnit4.class)
public class ProxyRobotsTest {

    private final static Logger logback = LoggerFactory.getLogger(ProxyRobotsTest.class);
    private static final String REGISTERED_ID = "BananaOne";
    private static final String RFID_VALUE = "11111111";
    private static final String PUSH_ADDRESS = "tcp://localhost:9000";
    private static final String SUB_ADDRESS = "tcp://localhost:9001";
    private final ConfigFactoryParameters configFactoryParameters = new ConfigFactoryParameters("/configurationTest.xml", EnumConfigFileType.RESOURCE);
    private final ZmqMessageBroker mockedZmqMessageBroker = createNiceMock(ZmqMessageBroker.class);
    private ConfigFactory configFactory;
    private RobotsMap robotsMap;
    @TestSubject
    private ProxyRobots myProxyRobots;
    @Mock
    private MockedTank mockedTank;

    @Before
    public void setUp() {
        logback.info("IN");

        // Build Mock of Tank
        mockedTank = new MockedTank();

        configFactory = ConfigFactory.createConfigFactory(configFactoryParameters);

        // Create the map with one mock tank
        robotsMap = new RobotsMap();
        robotsMap.add(mockedTank);

        logback.info("OUT");
    }

    private byte[] getMockRawZmqMessage(final IRobot iRobot, final EnumMessageType messageType) {
        final byte[] raw_zmq_message;
        byte[] specificMessage = new byte[0];
        String zmqMessageHeader = null;

        switch (messageType) {
            case REGISTERED:
                specificMessage = getBytesRegistered();
                zmqMessageHeader = iRobot.getRoutingId() + " " + "Registered" + " ";
                break;
            case INPUT:
                specificMessage = getBytesInput();
                zmqMessageHeader = iRobot.getRoutingId() + " " + "Input" + " ";
                break;
            default:
                logback.error("Case : Message type " + messageType + " not handled");
        }

        assert null != zmqMessageHeader;
        raw_zmq_message = Utils.Concatenate(zmqMessageHeader.getBytes(),
                specificMessage);

        return raw_zmq_message;
    }

    private byte[] getBytesRegistered() {
        final ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId(REGISTERED_ID);
        registeredBuilder.setTeam("BLUE");

        return registeredBuilder.build().toByteArray();
    }

    private byte[] getBytesInput() {
        final Controller.Input.Builder inputBuilder = Controller.Input.newBuilder();
        final Controller.Input.Fire.Builder fireBuilder = Controller.Input.Fire.newBuilder();
        final Controller.Input.Move.Builder moveBuilder = Controller.Input.Move.newBuilder();
        fireBuilder.setWeapon1(true);
        fireBuilder.setWeapon2(false);
        moveBuilder.setLeft(100);
        moveBuilder.setRight(0);
        inputBuilder.setFire(fireBuilder.build());
        inputBuilder.setMove(moveBuilder.build());

        return inputBuilder.build().toByteArray();
    }

    // Wait for a max timeout or for communicationService to stop
    private void waitForCloseOrTimeout() {
        long timeout = 0;

        // We use the value of the config for OutgoingMsgPeriod
        final long MAX_TIMEOUT = configFactory.getConfigProxy().getOutgoingMsgPeriod();
        while (myProxyRobots.isCommunicationServiceAlive() && MAX_TIMEOUT > timeout) {
            try {
                Thread.sleep(5);
                timeout += 5;
            } catch (final InterruptedException e) {
                logback.error(e.getMessage());
            }
        }
    }

    private void instantiateBasicProxyRobots() {
        // Build Mock of ZmqMessageBroker
        mockedZmqMessageBroker.addZmqMessageListener(anyObject(IZmqMessageListener.class));
        expectLastCall();

        expect(mockedZmqMessageBroker.sendZmqMessage((ZmqMessageBOM) anyObject())).andReturn(true).anyTimes();

        replay(mockedZmqMessageBroker);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageBroker, configFactory,
                robotsMap);
    }

    @Test
    public void testInitialiseTanks() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        assertEquals(1, myProxyRobots.robotsMap.getRobotsArray().size());
        assertEquals(mockedTank,
                myProxyRobots.robotsMap.get("tempRoutingId"));
        logback.info("OUT");
    }

    @Test
    public void testConnectToRobots() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        assertEquals(1, myProxyRobots.robotsMap.getConnectedRobots().size());
        logback.info("OUT");
    }

    @Test
    public void testRegisterFlow() throws Exception {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();
        assertEquals(EnumRegistrationState.NOT_REGISTERED, mockedTank.getRegistrationState());
        assertEquals("tempRoutingId", mockedTank.getRoutingId());

        myProxyRobots.startCommunicationService();

        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(ZmqMessageBOM.parseFrom(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

        assertEquals(EnumRegistrationState.REGISTERED, mockedTank.getRegistrationState());
        assertEquals("BananaOne", mockedTank.getRoutingId());

        logback.info("OUT");
    }


    @Test
    public void testUpdateConnectedTanks() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();
        assert (myProxyRobots.robotsMap.isRobotConnected("tempRoutingId"));
        myProxyRobots.startCommunicationService();

        myProxyRobots.sendRegister();

        // Tank is disconnected
        mockedTank.closeConnection();

        waitForCloseOrTimeout();

        // So the map of isConnected tanks is empty
        assertTrue(myProxyRobots.robotsMap.getConnectedRobots().isEmpty());

        logback.debug("OUT");
    }

    @Test
    public void testInitializeTanksFromConfig() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.initializeRobotsFromConfig();

        // We have two tanks: a mock and one initialized from the config file
        assertEquals(2, myProxyRobots.robotsMap.getNotConnectedRobots().size());
        // One tank in the map is indeed the one coming from the config file
        assertNotNull(myProxyRobots.robotsMap.get(configFactory.getConfigRobots().getConfigRobotsToRegister().get(0).getTempRoutingID()));

        logback.info("OUT");
    }

    @Test
    public void testSendServerRobotState() throws Exception {
        logback.info("IN");

        // Build Mock of ZmqMessageBroker
        final Capture<ZmqMessageBOM> captureMsg = new Capture<>();
        expect(mockedZmqMessageBroker.sendZmqMessage(capture(captureMsg))).andReturn(true).atLeastOnce();
        replay(mockedZmqMessageBroker);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageBroker, configFactory,
                robotsMap);

        myProxyRobots.connectToRobots();
        myProxyRobots.startCommunicationService();
        // Robot needs to be registered in order to send a ServerRobotState
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(ZmqMessageBOM.parseFrom(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

        // We put a new RFID value into the tank to change its state
        mockedTank.setRfidValue(RFID_VALUE);
        myProxyRobots.sendServerRobotStates();

        // ProxyRobot is expected to send a ServerRobotState message
        verify(mockedZmqMessageBroker);
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, captureMsg.getValue().getMessageType());
        assertEquals("RoutingId is supposed to have changed to the one provided by registered",
                REGISTERED_ID, captureMsg.getValue().getRoutingId());

        logback.info("OUT");
    }

    @Test
    public void testStart_noUdpDiscovery() {
        logback.info("IN");

        // Build Mock of ZmqMessageBroker
        final Capture<String> capturePushAddress = new Capture<>();
        final Capture<String> captureSubscribeAddress = new Capture<>();
        expect(mockedZmqMessageBroker.connectToServer(capture(capturePushAddress), capture(captureSubscribeAddress))).andReturn(true);
        mockedZmqMessageBroker.close();
        expectLastCall().once();
        replay(mockedZmqMessageBroker);

        // We are testing the real class, so we do not want to lose time
        // trying to connect to robots by bluetooth
        // Hence we provide an empty config file
        final ConfigFactoryParameters localConfigFactoryParameters = new ConfigFactoryParameters("/configurationTest_NoRobots.xml", EnumConfigFileType.RESOURCE);
        configFactory = ConfigFactory.createConfigFactory(localConfigFactoryParameters);

        // Instantiate main class with mock parameters
        // We build an empty robot map
        myProxyRobots = new ProxyRobots(mockedZmqMessageBroker, configFactory,
                new RobotsMap());

        myProxyRobots.start();

        waitForCloseOrTimeout();
        // Map contains only one tank from the config file,
        // this tank fails to connect because of wrong settings, so
        // the communication service should quickly stop and close
        // the message framework proxy
        verify(mockedZmqMessageBroker);

        // messageBroker.connectToServer() was called with parameters
        // coming from the configuration file
        assertEquals(configFactory.getMaxPriorityConfigServerGame().getPushAddress(),
                capturePushAddress.getValue());
        assertEquals(configFactory.getMaxPriorityConfigServerGame().getSubscribeAddress(),
                captureSubscribeAddress.getValue());

        logback.info("OUT");
    }

    @Test
    public void testStart_udpDiscovery() {
        logback.info("IN");

        // Build Mock of ZmqMessageBroker
        final Capture<String> capturePushAddress = new Capture<>();
        final Capture<String> captureSubscribeAddress = new Capture<>();
        expect(mockedZmqMessageBroker.connectToServer(capture(capturePushAddress), capture(captureSubscribeAddress))).andReturn(true);
        mockedZmqMessageBroker.close();
        expectLastCall().once();
        replay(mockedZmqMessageBroker);

        // We are testing the real class, so we do not want to lose time
        // trying to connect to robots by bluetooth
        // Hence we provide an empty config file
        final ConfigFactoryParameters localConfigFactoryParameters = new ConfigFactoryParameters("/configurationTest_NoRobots.xml", EnumConfigFileType.RESOURCE);
        configFactory = ConfigFactory.createConfigFactory(localConfigFactoryParameters);

        final UdpBeaconFinder udpBeaconFinder = createNiceMock(UdpBeaconFinder.class);
        expect(udpBeaconFinder.hasFoundServer()).andReturn(true).once();
        expect(udpBeaconFinder.getPushAddress()).andReturn(PUSH_ADDRESS).once();
        expect(udpBeaconFinder.getSubscribeAddress()).andReturn(SUB_ADDRESS).once();
        replay(udpBeaconFinder);

        // Instantiate main class with mock parameters
        // We build an empty robot map
        myProxyRobots = new ProxyRobots(udpBeaconFinder, mockedZmqMessageBroker,
                configFactory, new RobotsMap());

        myProxyRobots.start();

        waitForCloseOrTimeout();
        // Map contains only one tank from the config file,
        // this tank fails to connect because of wrong settings, so
        // the communication service should quickly stop and close
        // the message framework proxy
        verify(mockedZmqMessageBroker);

        // Check that udpBeacon has been correctly used (broadcastAndGetServerAddress() and
        // hasFoundServer() were called)
        verify(udpBeaconFinder);

        // messageBroker.connectToServer() was called with parameters
        // provided by udpBeaconFinder
        assertEquals(PUSH_ADDRESS, capturePushAddress.getValue());
        assertEquals(SUB_ADDRESS, captureSubscribeAddress.getValue());
        logback.info("OUT");
    }

    @Test
    public void testOnInput() throws Exception {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        myProxyRobots.startCommunicationService();

        // Robot needs to be registered in order to receive Input messages
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(ZmqMessageBOM.parseFrom(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

        // Tank has for now no Input registered
        assertFalse(((MockedTank) myProxyRobots.robotsMap.get("BananaOne")).getInputFire().hasFire());
        assertFalse(((MockedTank) myProxyRobots.robotsMap.get("BananaOne")).getInputMove().hasMove());

        // Now simulate reception of a INPUT message
        myProxyRobots.receivedNewZmq(ZmqMessageBOM.parseFrom(getMockRawZmqMessage(mockedTank, EnumMessageType.INPUT)));

        // Tank received the right Input correctly
        assertTrue(((MockedTank) myProxyRobots.robotsMap.get("BananaOne")).getInputFire().hasFire());
        assertTrue(((MockedTank) myProxyRobots.robotsMap.get("BananaOne")).getInputMove().hasMove());

        logback.info("OUT");
    }

    @Test
    public void testGetNbOutgoingMessageFiltered() {
        logback.info("IN");

        instantiateBasicProxyRobots();
        myProxyRobots.start();

        // We run the proxy for maxTimeoutMs
        waitForCloseOrTimeout();

        // Since we wait for a timeout as long as outgoingMessagePeriod
        // during which the proxy runs and tries to send messages,
        // we should filter some messages
        logback.debug("getNbOutgoingMessageFiltered: " + myProxyRobots.outgoingMessageFiltered);
        assertTrue("There should be at least one filtered message", 0 < myProxyRobots.outgoingMessageFiltered);

        logback.info("OUT");
    }

    @Test
    public void testProxyRobots_StartWithMockTank() throws Exception {
        instantiateBasicProxyRobots();
        myProxyRobots.start();

        // We run the proxy for maxTimeoutMs
        waitForCloseOrTimeout();
    }

    @After
    public void tearDown() {
        myProxyRobots.stop();
    }
}
