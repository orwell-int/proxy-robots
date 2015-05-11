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
import orwell.proxy.robot.IRobot;
import orwell.proxy.robot.RobotsMap;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ZmqMessageBOM;
import orwell.proxy.zmq.ZmqMessageBroker;
import orwell.proxy.zmq.ZmqMessageDecoder;

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
    private final static long MAX_TIMEOUT_MS = 500;
    private static final String REGISTERED_ID = "BananaOne";
    private final ConfigFactoryParameters configFactoryParameters = new ConfigFactoryParameters("/configurationTest.xml", EnumConfigFileType.RESOURCE);
    private final ZmqMessageBroker mockedZmqMessageFramework = createNiceMock(ZmqMessageBroker.class);
    private ConfigFactory configFactory;
    private RobotsMap robotsMap;
    @TestSubject
    private ProxyRobots myProxyRobots;
    @Mock
    //	private Tank myTank;
    private MockedTank mockedTank;

    @Before
    public void setUp() {
        logback.info("IN");

        // Build Mock of Tank
        mockedTank = new MockedTank();

        configFactory = new ConfigFactory(configFactoryParameters);

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
        fireBuilder.setWeapon1(false);
        fireBuilder.setWeapon2(false);
        moveBuilder.setLeft(0);
        moveBuilder.setRight(0);
        inputBuilder.setFire(fireBuilder.build());
        inputBuilder.setMove(moveBuilder.build());

        return inputBuilder.build().toByteArray();
    }


    private void waitForCloseOrTimeout() {
        long timeout = 0;
        while (myProxyRobots.isCommunicationServiceAlive() && MAX_TIMEOUT_MS > timeout) {
            try {
                //noinspection BusyWait
                Thread.sleep(5);
                timeout += 5;
            } catch (final InterruptedException e) {
                logback.error(e.getMessage());
            }
        }
    }

    private void instantiateBasicProxyRobots() {
        // Build Mock of ZmqMessageBroker
        mockedZmqMessageFramework.addZmqMessageListener(anyObject(IZmqMessageListener.class));
        expectLastCall();

        expect(mockedZmqMessageFramework.sendZmqMessage((ZmqMessageBOM) anyObject())).andReturn(true).anyTimes();

        replay(mockedZmqMessageFramework);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework, configFactory,
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

        // So the map of isConnected tanks is empty
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

        // Build Mock of ZmqMessageBroker
        final Capture<ZmqMessageBOM> captureMsg = new Capture<>();
        expect(mockedZmqMessageFramework.sendZmqMessage(capture(captureMsg))).andReturn(true).atLeastOnce();
        replay(mockedZmqMessageFramework);

        // Instantiate main class with mock parameters
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework, configFactory,
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

        // Build Mock of ZmqMessageBroker
        mockedZmqMessageFramework.close();
        expectLastCall().once();
        replay(mockedZmqMessageFramework);

        // We are testing the real class, so we do not want to lose time
        // trying to connect to robots by bluetooth
        // Hence we provide an empty config file
        final ConfigFactoryParameters localConfigFactoryParameters = new ConfigFactoryParameters("/configurationTest_NoRobots.xml", EnumConfigFileType.RESOURCE);
        configFactory = new ConfigFactory(localConfigFactoryParameters);

        // Instantiate main class with mock parameters
        // We build an empty robot map
        myProxyRobots = new ProxyRobots(mockedZmqMessageFramework, configFactory,
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
