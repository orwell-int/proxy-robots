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
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.elements.ConfigRobotException;
import orwell.proxy.config.source.ConfigurationResource;
import orwell.proxy.mock.MockedTank;
import orwell.proxy.robot.EnumConnectionState;
import orwell.proxy.robot.EnumRegistrationState;
import orwell.proxy.robot.EnumRobotVictoryState;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.udp.UdpBeaconFinder;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ServerGameMessageBroker;
import orwell.proxy.zmq.ZmqMessageBOM;

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
    private static final String RFID_VALUE_1 = "11111111";
    private static final String PUSH_ADDRESS_UDP = "tcp://localhost:9000";
    private static final String SUB_ADDRESS_UDP = "tcp://localhost:9001";
    private static final String PUSH_ADDRESS_CONFIG = "tcp://127.0.0.1:9001";
    private static final String SUB_ADDRESS_CONFIG = "tcp://127.0.0.1:9000";
    private static final String CONFIGURATION_RESOURCE_PATH = "/configurationTest.xml";
    private static final long WAIT_TIMEOUT_MS = 500; // has to be greater than ProxyRobots.THREAD_SLEEP_MS
    private static final long RECEIVE_TIMEOUT = 500;
    private static final String ROUTING_ID_ALL = "all_robots";
    private ServerGameMessageBroker mockedServerGameMessageBroker;
    private ConfigFactory configFactory;
    private RobotsMap robotsMap;
    private ConfigurationResource configuration;
    @TestSubject
    private ProxyRobots myProxyRobots;
    @Mock
    private MockedTank mockedTank;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        configuration = new ConfigurationResource(CONFIGURATION_RESOURCE_PATH);
        mockedServerGameMessageBroker = createNiceMock(ServerGameMessageBroker.class);

        // Build Mock of Tank
        mockedTank = new MockedTank();

        configFactory = ConfigFactory.createConfigFactory(configuration);

        // Create the map with one mock tank
        robotsMap = new RobotsMap();
        robotsMap.add(mockedTank);
    }

    private byte[] getBytesGameState_Playing() {
        return ProtobufTest.getTestGameState_Playing().toByteArray();
    }

    private byte[] getBytesGameState_Winner() {
        return ProtobufTest.getTestGameState_Winner().toByteArray();
    }

    private byte[] getBytesRegistered() {
        return ProtobufTest.getTestRegistered().toByteArray();
    }

    private byte[] getBytesInput() {
        return ProtobufTest.getTestInput().toByteArray();
    }

    // Wait for a max timeout or for communicationService to stop
    private void waitForCloseOrTimeout(final long timeoutMs) {
        long timeout = 0;

        while (myProxyRobots.isCommunicationServiceAlive() && timeoutMs > timeout) {
            try {
                Thread.sleep(5);
                timeout += 5;
            } catch (final InterruptedException e) {
                logback.error(e.getMessage());
            }
        }
    }

    private void instantiateBasicProxyRobots() {
        // Build Mock of ServerGameMessageBroker
        mockedServerGameMessageBroker.addZmqMessageListener(anyObject(IZmqMessageListener.class));
        expectLastCall();

        expect(mockedServerGameMessageBroker.sendZmqMessage((ZmqMessageBOM) anyObject())).andReturn(true).anyTimes();
        expect(mockedServerGameMessageBroker.isConnectedToServer()).andReturn(true).anyTimes();

        replay(mockedServerGameMessageBroker);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedServerGameMessageBroker, configFactory,
                robotsMap);
    }

    @Test
    public void testInitialiseTanks() {
        instantiateBasicProxyRobots();

        assertEquals(1, myProxyRobots.robotsMap.getRobotsArray().size());
        assertEquals(mockedTank,
                myProxyRobots.robotsMap.get("tempRoutingId"));
    }

    @Test
    public void testConnectToRobots() {
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        assertEquals(1, myProxyRobots.robotsMap.getConnectedRobots().size());
    }

    @Test
    public void testRegisterFlow() throws Exception {
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();
        assertEquals(EnumRegistrationState.NOT_REGISTERED, mockedTank.getRegistrationState());
        assertEquals("tempRoutingId", mockedTank.getRoutingId());

        myProxyRobots.startCommunicationService();

        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.REGISTERED,
                        getBytesRegistered())
        );

        assertEquals(EnumRegistrationState.REGISTERED, mockedTank.getRegistrationState());
        assertEquals(ProtobufTest.REGISTERED_ROUTING_ID, mockedTank.getRoutingId());
    }


    @Test
    public void testUpdateConnectedTanks() {
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();
        assert (myProxyRobots.robotsMap.isRobotConnected("tempRoutingId"));
        myProxyRobots.startCommunicationService();

        myProxyRobots.sendRegister();

        // Tank is disconnected
        mockedTank.closeConnection();

        waitForCloseOrTimeout(WAIT_TIMEOUT_MS);

        // So the map of isConnected tanks is empty
        assertTrue(myProxyRobots.robotsMap.getConnectedRobots().isEmpty());
    }

    @Test
    public void testInitializeTanksFromConfig() throws ConfigRobotException {
        instantiateBasicProxyRobots();

        myProxyRobots.initializeRobotsFromConfig();

        // We have two tanks: a mock and one initialized from the config file
        assertEquals(2, myProxyRobots.robotsMap.getNotConnectedRobots().size());
        // One tank in the map is indeed the one coming from the config file
        assertNotNull(myProxyRobots.robotsMap.get(configFactory.getConfigRobots().getConfigRobotsToRegister().get(0).getTempRoutingID()));
    }

    @Test
    public void testSendServerRobotState() throws Exception {
        logback.debug(">>>>>>>>> IN");

        // Build Mock of ServerGameMessageBroker
        final Capture<ZmqMessageBOM> captureMsg = new Capture<>();
        expect(mockedServerGameMessageBroker.sendZmqMessage(capture(captureMsg))).andReturn(true).atLeastOnce();
        replay(mockedServerGameMessageBroker);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedServerGameMessageBroker, configFactory,
                robotsMap);

        myProxyRobots.connectToRobots();
        myProxyRobots.startCommunicationService();
        // Robot needs to be registered in order to send a ServerRobotState
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.REGISTERED,
                        getBytesRegistered())
        );
        // We put a new RFID value into the tank to change its state
        mockedTank.setRfidValue(RFID_VALUE_1);
        myProxyRobots.sendServerRobotStates();

        // ProxyRobot is expected to send a ServerRobotState message
        verify(mockedServerGameMessageBroker);
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, captureMsg.getValue().getMessageType());
        assertEquals("RoutingId is supposed to have changed to the one provided by registered",
                ProtobufTest.REGISTERED_ROUTING_ID, captureMsg.getValue().getRoutingId());
    }

    @Test
    public void testStart_noUdpDiscovery() throws ConfigRobotException {
        logback.debug(">>>>>>>>> IN");

        // Build Mock of ServerGameMessageBroker
        final Capture<String> capturePushAddress = new Capture<>();
        final Capture<String> captureSubscribeAddress = new Capture<>();
        expect(mockedServerGameMessageBroker.connectToServer(capture(capturePushAddress), capture(captureSubscribeAddress))).andReturn(true);
        mockedServerGameMessageBroker.close();
        expectLastCall().once();
        replay(mockedServerGameMessageBroker);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedServerGameMessageBroker, configFactory,
                robotsMap);

        myProxyRobots.start();

        waitForCloseOrTimeout(WAIT_TIMEOUT_MS);
        // Map contains only one tank from the config file,
        // this tank fails to connect because of wrong settings, so
        // the communication service should quickly stop and close
        // the message framework proxy
        verify(mockedServerGameMessageBroker);

        // messageBroker.connectToServer() was called with parameters
        // coming from the configuration file
        assertEquals(configFactory.getMaxPriorityConfigServerGame().getPushAddress(),
                capturePushAddress.getValue());
        assertEquals(configFactory.getMaxPriorityConfigServerGame().getSubscribeAddress(),
                captureSubscribeAddress.getValue());
    }

    @Test
    public void testStart_udpDiscovery() throws ConfigRobotException {
        // Build Mock of ServerGameMessageBroker
        final Capture<String> capturePushAddress = new Capture<>();
        final Capture<String> captureSubscribeAddress = new Capture<>();
        expect(mockedServerGameMessageBroker.connectToServer(capture(capturePushAddress), capture(captureSubscribeAddress))).andReturn(true);
        mockedServerGameMessageBroker.close();
        expectLastCall().once();
        replay(mockedServerGameMessageBroker);

        // Simulate a working UDP beacon finder
        final UdpBeaconFinder udpBeaconFinder = createNiceMock(UdpBeaconFinder.class);
        expect(udpBeaconFinder.hasFoundServer()).andReturn(true).once();
        expect(udpBeaconFinder.getPushAddress()).andReturn(PUSH_ADDRESS_UDP).once();
        expect(udpBeaconFinder.getSubscribeAddress()).andReturn(SUB_ADDRESS_UDP).once();
        replay(udpBeaconFinder);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(udpBeaconFinder, mockedServerGameMessageBroker,
                configFactory, robotsMap);

        myProxyRobots.start();

        waitForCloseOrTimeout(WAIT_TIMEOUT_MS);
        // Map contains only one tank from the config file,
        // this tank fails to connect because of wrong settings, so
        // the communication service should quickly stop and close
        // the message framework proxy
        verify(mockedServerGameMessageBroker);

        // Check that udpBeacon has been correctly used (broadcastAndGetServerAddress() and
        // hasFoundServer() were called)
        verify(udpBeaconFinder);

        // messageBroker.connectToServer() was called with parameters
        // provided by udpBeaconFinder
        assertEquals(PUSH_ADDRESS_UDP, capturePushAddress.getValue());
        assertEquals(SUB_ADDRESS_UDP, captureSubscribeAddress.getValue());
    }

    @Test
    /**
     * Checks that if UDP discovery fails or if we make 0 attempts to find the
     * server with the beacon finder, then we fallback on configuration data
     * found in the xml file
     */
    public void testStart_udpDiscoveryWithZeroAttempt() throws ConfigRobotException {
        // Build Mock of ServerGameMessageBroker
        final Capture<String> capturePushAddress = new Capture<>();
        final Capture<String> captureSubscribeAddress = new Capture<>();
        expect(mockedServerGameMessageBroker.connectToServer(capture(capturePushAddress), capture(captureSubscribeAddress))).andReturn(true);
        mockedServerGameMessageBroker.close();
        expectLastCall().once();
        replay(mockedServerGameMessageBroker);

        // Simulate a failure of the UDP beacon finder
        // (or that we set 'attempts' to 0)
        final UdpBeaconFinder udpBeaconFinder = createNiceMock(UdpBeaconFinder.class);
        expect(udpBeaconFinder.hasFoundServer()).andReturn(false).once();
        replay(udpBeaconFinder);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(udpBeaconFinder, mockedServerGameMessageBroker,
                configFactory, robotsMap);

        myProxyRobots.start();

        waitForCloseOrTimeout(WAIT_TIMEOUT_MS * 100);
        // Map contains only one tank from the config file,
        // this tank fails to connect because of wrong settings, so
        // the communication service should quickly stop and close
        // the message framework proxy
        verify(mockedServerGameMessageBroker);

        // Check that udpBeacon has been correctly used (broadcastAndGetServerAddress() and
        // hasFoundServer() were called)
        verify(udpBeaconFinder);

        // messageBroker.connectToServer() was called with parameters
        // provided by udpBeaconFinder
        assertEquals(PUSH_ADDRESS_CONFIG, capturePushAddress.getValue());
        assertEquals(SUB_ADDRESS_CONFIG, captureSubscribeAddress.getValue());
    }

    @Test
    public void testOnInput() throws Exception {
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        myProxyRobots.startCommunicationService();

        // Robot needs to be registered in order to receive Input messages
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.REGISTERED,
                        getBytesRegistered())
        );
        // Tank has for now no Input registered
        assertFalse(((MockedTank) myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).
                getInputFire().hasFire());
        assertFalse(((MockedTank) myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).
                getInputMove().hasMove());

        // Now simulate reception of a INPUT message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.INPUT,
                        getBytesInput())
        );
        // Tank received the right Input correctly
        assertTrue(((MockedTank) myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).
                getInputFire().hasFire());
        assertTrue(((MockedTank) myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).
                getInputMove().hasMove());
    }

    @Test
    public void onInput_MessageNotSentException() throws Exception {
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        assertEquals(EnumConnectionState.CONNECTED, mockedTank.getConnectionState());

        myProxyRobots.startCommunicationService();

        // Robot needs to be registered in order to receive Input messages
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.REGISTERED,
                        getBytesRegistered())
        );

        // Make robot unable to send unit message
        ((MockedTank) myProxyRobots.robotsMap.get(mockedTank.getRoutingId())).makeUnableToSendUnitMessages();

        // Now simulate reception of a INPUT message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.INPUT,
                        getBytesInput())
        );

        // Robot should be disconnected by the proxy,
        // because proxy failed to send it an input
        assertEquals(EnumConnectionState.NOT_CONNECTED, mockedTank.getConnectionState());
    }

    @Test
    public void testGetNbOutgoingMessageFiltered() throws ConfigRobotException {
        instantiateBasicProxyRobots();
        myProxyRobots.start();

        // We run the proxy for WAIT_TIMEOUT_MS
        waitForCloseOrTimeout(WAIT_TIMEOUT_MS);

        // Since we wait for a timeout as long as outgoingMessagePeriod
        // during which the proxy runs and tries to send messages,
        // we should filter some messages
        assertTrue("There should be at least one filtered message", 0 < myProxyRobots.getOutgoingMessageFiltered());
    }

    @Test
    /**
     * Just run the proxy, with a mock Tank, to check it starts and ends well
     */
    public void testProxyRobots_StartWithMockTank() throws Exception {
        // Instantiate main class with mock tank, but real zmq conf
        myProxyRobots = new ProxyRobots(new ServerGameMessageBroker(RECEIVE_TIMEOUT, 1000, 1000), configFactory,
                robotsMap);
        myProxyRobots.start();

        // We run the proxy for WAIT_TIMEOUT_MS
        waitForCloseOrTimeout(WAIT_TIMEOUT_MS);
    }

    @Test
    public void testOnGameState() throws Exception {
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        myProxyRobots.startCommunicationService();

        // Robot needs to be registered in order to receive Input messages
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(mockedTank.getRoutingId(),
                        EnumMessageType.REGISTERED,
                        getBytesRegistered())
        );

        // Game is still playing, there is then no winner
        assertEquals(EnumRobotVictoryState.WAITING_FOR_START, (myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).getVictoryState()
        );

        // Now simulate reception of a GAME_STATE message
        // saying the game is now playing
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(ROUTING_ID_ALL,
                        EnumMessageType.GAME_STATE,
                        getBytesGameState_Playing()
                )
        );
        // Tank victory state was changed accordingly to PLAYING
        assertEquals(EnumRobotVictoryState.PLAYING, (myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).getVictoryState()
        );

        // Now simulate reception of a GAME_STATE message
        // Where BLUE Team is the winner
        myProxyRobots.receivedNewZmq(
                new ZmqMessageBOM(ROUTING_ID_ALL,
                        EnumMessageType.GAME_STATE,
                        getBytesGameState_Winner()
                )
        );

        // Tank victory state was changed accordingly
        assertEquals(EnumRobotVictoryState.WINNER, (myProxyRobots.robotsMap.
                get(ProtobufTest.REGISTERED_ROUTING_ID)).getVictoryState()
        );
    }

    @After
    public void tearDown() {
        myProxyRobots.stop();
        logback.debug("<<<< OUT");
    }
}
