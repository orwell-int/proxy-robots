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
import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.EnumConfigFileType;
import orwell.proxy.mock.MockedTank;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


/**
 * Tests for {@link ProxyRobots}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */


@RunWith(JUnit4.class)
public class ProxyRobotsTest {

    final static Logger logback = LoggerFactory.getLogger(ProxyRobotsTest.class);
    final static long MAX_TIMEOUT_MS = 500;
    private static final String REGISTERED_ID = "BananaOne";
    private final ConfigCli configCli = new ConfigCli("/configurationTest.xml", EnumConfigFileType.RESOURCE);
    private ConfigFactory configFactory;
    private RobotsMap robotsMap;

    @TestSubject
    private ProxyRobots myProxyRobots;

    @Mock
    //	private Tank myTank;
    private MockedTank mockedTank;
    private final ZmqMessageFramework mockedZmqMessageFramework = createNiceMock(ZmqMessageFramework.class);

    @Before
    public void setUp() throws Exception { //expectPrivate might throw exceptions
        logback.info("IN");

        // Build Mock of Tank
        mockedTank = new MockedTank();

        configFactory = new ConfigFactory(configCli);

        // Create the map with one mock tank
        robotsMap = new RobotsMap();
        robotsMap.add(mockedTank);

        logback.info("OUT");
    }

    public byte[] getMockRawZmqMessage(IRobot iRobot, EnumMessageType messageType) {
        byte[] raw_zmq_message;
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

        raw_zmq_message = Utils.Concatenate(zmqMessageHeader.getBytes(),
                specificMessage);

        return raw_zmq_message;
    }

    public byte[] getBytesRegistered() {
        ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId(REGISTERED_ID);
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


    public void waitForCloseOrTimeout() {
        long timeout = 0;
        while (myProxyRobots.isCommunicationServiceAlive() && timeout < MAX_TIMEOUT_MS) {
            try {
                Thread.sleep(5);
                timeout += 5;
            } catch (InterruptedException e) {
                logback.error(e.getStackTrace().toString());
            }
        }
    }

    public void instantiateBasicProxyRobots() {
        // Build Mock of ZmqMessageFramework
        mockedZmqMessageFramework.addZmqMessageListener(anyObject(IZmqMessageListener.class));
        expectLastCall();

        expect(mockedZmqMessageFramework.sendZmqMessage((ZmqMessageBOM) anyObject())).andReturn(true).anyTimes();

        replay(mockedZmqMessageFramework);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework,
                configFactory.getConfigServerGame(),
                configFactory.getConfigRobots(),
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
    public void testRegisterFlow() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();
        assertEquals(IRobot.EnumRegistrationState.NOT_REGISTERED, mockedTank.getRegistrationState());
        assertEquals("tempRoutingId", mockedTank.getRoutingId());

        myProxyRobots.startCommunicationService();

        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(new ZmqMessageDecoder(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

        assertEquals(IRobot.EnumRegistrationState.REGISTERED, mockedTank.getRegistrationState());
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

        // So the map of connected tanks is empty
        assert (myProxyRobots.robotsMap.getConnectedRobots().isEmpty());

        logback.debug("OUT");
    }

    @Test
    public void testInitializeTanksFromConfig() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.initializeTanksFromConfig();

        // We have two tanks: a mock and one initialized from the config file
        assertEquals(2, myProxyRobots.robotsMap.getNotConnectedRobots().size());
        // One tank in the map is indeed the one coming from the config file
        assertNotNull(myProxyRobots.robotsMap.get(configFactory.getConfigRobots().getConfigRobotsToRegister().get(0).getTempRoutingID()));

        logback.info("OUT");
    }

    @Test
    public void testSendServerRobotState() {
        logback.info("IN");

        // Build Mock of ZmqMessageFramework
        final Capture<ZmqMessageBOM> captureMsg = new Capture<>();
        expect(mockedZmqMessageFramework.sendZmqMessage(capture(captureMsg))).andReturn(true).atLeastOnce();
        replay(mockedZmqMessageFramework);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework,
                configFactory.getConfigServerGame(),
                configFactory.getConfigRobots(),
                robotsMap);

        myProxyRobots.connectToRobots();
        myProxyRobots.startCommunicationService();
        // Robot needs to be registered in order to send a ServerRobotState
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(new ZmqMessageDecoder(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

        myProxyRobots.sendServerRobotStates();

        // ProxyRobot is expected to send a ServerRobotState message
        verify(mockedZmqMessageFramework);
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, captureMsg.getValue().getMsgType());
        assertEquals("RoutingId is supposed to have changed to the one provided by registered",
                REGISTERED_ID, captureMsg.getValue().getRoutingId());

        logback.info("OUT");
    }

    @Test
    public void testStart() {
        logback.info("IN");

        // Build Mock of ZmqMessageFramework
        mockedZmqMessageFramework.close();
        expectLastCall().once();
        replay(mockedZmqMessageFramework);

        // Instantiate main class with mock parameters
        // We build an empty robot map
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework,
                configFactory.getConfigServerGame(),
                configFactory.getConfigRobots(),
                new RobotsMap());

        myProxyRobots.start();

        waitForCloseOrTimeout();
        // Map contains only one tank from the config file,
        // this tank fails to connect because of wrong settings, so
        // the communication service should quickly stop and close
        // the message framework proxy
        verify(mockedZmqMessageFramework);

        logback.info("OUT");
    }


    @Test
    public void testOnInput() {
        logback.info("IN");
        instantiateBasicProxyRobots();

        myProxyRobots.connectToRobots();

        myProxyRobots.startCommunicationService();

        // Robot needs to be registered in order to receive Input messages
        myProxyRobots.sendRegister();
        // Simulate reception of a REGISTERED message
        myProxyRobots.receivedNewZmq(new ZmqMessageDecoder(getMockRawZmqMessage(mockedTank, EnumMessageType.REGISTERED)));

        // Now simulate reception of a INPUT message
        myProxyRobots.receivedNewZmq(new ZmqMessageDecoder(getMockRawZmqMessage(mockedTank, EnumMessageType.INPUT)));

        // Tank received the right Input correctly
        assertArrayEquals(getBytesInput(),
                ((MockedTank) myProxyRobots.robotsMap.get("BananaOne")).getControllerInputBytes());

        logback.info("OUT");
    }

    @After
    public void tearDown() {
        myProxyRobots.stop();
    }
}
