package orwell.proxy.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.elements.*;
import orwell.proxy.config.source.ConfigurationFile;
import orwell.proxy.config.source.ConfigurationResource;
import orwell.proxy.config.source.NotFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ConfigurationTest {

    public static final int BEGIN_PORT = 10000;
    public static final int PORTS_COUNT = 8;
    private final static Logger logback = LoggerFactory.getLogger(ConfigurationTest.class);
    private static final String CONFIGURATION_RESOURCE_PATH = "/configurationTest.xml";
    private static final int PUSH_PORT = 9001;
    private static final int SUB_PORT = 9000;
    private static final int CAMERA_PORT_TANK = 9100;
    private static final int CAMERA_PORT_SCOUT = 9102;
    private static final int LINGER_TIME_MS = 1000;
    private static final int OUTGOING_MSG_FREQ_MS = 500;
    private static final int UDP_SERVER_GAME_FINDER_TIMEOUT = 1000;
    private static final int UDP_SERVER_GAME_FINDER_ATTEMPTS = 5;
    private static final int UDP_SERVER_GAME_FINDER_PORT = 9080;
    private static final String SERVER_ADDRESS = "tcp://127.0.0.1:";
    private static final int UDP_PROXY_BROADCAST_PORT = 9081;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        assertNotNull("Test resource missing", getClass().getResource(CONFIGURATION_RESOURCE_PATH));
    }

    private ConfigurationResource getConfigurationResourceTest() {
        return new ConfigurationResource(CONFIGURATION_RESOURCE_PATH);
    }

    @Test
    public void testPopulateConfigModel() {
        assertTrue(getConfigurationResourceTest().isPopulated());
    }

    @Test
    public void testProxyList() {
        final ConfigProxy configProxy;
        configProxy = getConfigurationResourceTest().getConfigModel()
                .getConfigProxy();

        assertEquals(3, configProxy.getConfigServerGames().size());
        try {
            assertNotNull(configProxy.getMaxPriorityConfigServerGame());
        } catch (final Exception e) {
            logback.error(e.getMessage());
        }

        assertEquals("localhost", configProxy.getConfigServerGames().get(2)
                .getName());
    }

    @Test
    public void testServerGameElement() {
        final ConfigServerGame configServerGame;
        try {
            configServerGame = getConfigurationResourceTest().getConfigModel()
                    .getConfigProxy().getMaxPriorityConfigServerGame();
            assertEquals(SERVER_ADDRESS + PUSH_PORT, configServerGame.getPushAddress());
            assertEquals(SERVER_ADDRESS + SUB_PORT, configServerGame.getSubscribeAddress());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testConfigProxy() {
        final ConfigProxy configProxy;
        configProxy = getConfigurationResourceTest().getConfigModel()
                .getConfigProxy();

        assertEquals(UDP_PROXY_BROADCAST_PORT, configProxy.getUdpProxyBroadcastPort());
        assertEquals(LINGER_TIME_MS, configProxy.getSenderLinger());
        assertEquals(LINGER_TIME_MS, configProxy.getReceiverLinger());
        assertEquals(OUTGOING_MSG_FREQ_MS, configProxy.getOutgoingMsgPeriod());
    }

    @Test
    public void testRobotsList() {
        final ConfigRobots configRobots;
        configRobots = getConfigurationResourceTest().getConfigModel()
                .getConfigRobots();

        assertEquals(2, configRobots.getConfigTanks().size());
        try {
            assertNotNull(configRobots.getConfigRobot("BananaOne"));
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testTankElement() {
        final ConfigTank configTank;
        try {
            configTank = (ConfigTank) getConfigurationResourceTest().getConfigModel().getConfigRobots()
                    .getConfigRobot("BananaOne");
            assertEquals("001653119482", configTank.getBluetoothID());
            assertEquals("Daneel", configTank.getBluetoothName());
            assertNotNull(configTank.getConfigCamera());
            assertEquals("192.168.1.50", configTank.getConfigCamera().getIp());
            assertEquals(CAMERA_PORT_TANK, configTank.getConfigCamera().getPort());
            assertEquals("/videofeed", configTank.getConfigCamera().getResourcePath());
            assertEquals("Yellow hull -- TO BE BETTER CONFIGURED", configTank.getImage());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testScoutElement() {
        final ConfigScout configScout;
        try {
            configScout = (ConfigScout) getConfigurationResourceTest().getConfigModel().getConfigRobots()
                    .getConfigRobot("ScoutOne");
            assertNotNull(configScout.getConfigCamera());
            assertEquals("192.168.1.52", configScout.getConfigCamera().getIp());
            assertEquals(CAMERA_PORT_SCOUT, configScout.getConfigCamera().getPort());
            assertEquals("/videofeed", configScout.getConfigCamera().getResourcePath());
            assertEquals("Red hull -- TO BE BETTER CONFIGURED", configScout.getImage());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testRobotsToRegister() {
        final ConfigRobots configRobots;
        configRobots = getConfigurationResourceTest().getConfigModel()
                .getConfigRobots();

        assertEquals(2, configRobots.getConfigRobotsToRegister().size());
    }

    @Test
    public void testConfigurationFile_fail() throws FileNotFoundException, NotFileException {
        File file = null;
        try {
            // Create empty file
            file = File.createTempFile("testConfigurationFile_fail", ".tmp");
        } catch (final IOException e) {
            fail(e.toString());
        }

        // Population fails since file is empty
        assertFalse((new ConfigurationFile(file.getAbsolutePath())).isPopulated());
    }

    @Test
    public void testConfigurationFile_success() throws IOException {
        File file = null;
        try {
            // Create empty temp file
            file = File.createTempFile("testConfigurationFile_success", ".tmp");
            // copy content of resource coming from jar to temp file
            final InputStream in = getClass().getResourceAsStream(CONFIGURATION_RESOURCE_PATH);
            Files.copy(in, file.toPath(), REPLACE_EXISTING);
        } catch (final IOException e) {
            fail(e.toString());
        }

        // Population succeed since file is filed with complete data
        assertTrue((new ConfigurationFile(file.getAbsolutePath())).isPopulated());
    }

    @Test
    public void testUdpServerGameFinderElement() {
        final ConfigUdpServerGameFinder configUdpServerGameFinder;
        try {
            configUdpServerGameFinder = getConfigurationResourceTest().getConfigModel()
                    .getConfigProxy().getConfigUdpServerGameFinder();
            assertEquals(UDP_SERVER_GAME_FINDER_PORT, configUdpServerGameFinder.getPort());
            assertEquals(UDP_SERVER_GAME_FINDER_ATTEMPTS, configUdpServerGameFinder.getAttempts());
            assertEquals(UDP_SERVER_GAME_FINDER_TIMEOUT, configUdpServerGameFinder.getTimeoutPerAttemptMs());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testConfiguration_loadsDefaultResource() {
        assertTrue(new ConfigurationResource("configThatDoesNotExist.xml").isPopulated());
    }

    @Test
    public void testConfigRobotsPortsPool() {
        final ConfigRobotsPortsPool configRobotsPortsPool;
        configRobotsPortsPool = getConfigurationResourceTest().getConfigModel()
                .getConfigProxy().getConfigRobotsPortsPool();

        assertEquals(BEGIN_PORT, configRobotsPortsPool.getBeginPort());
        assertEquals(PORTS_COUNT, configRobotsPortsPool.getPortsCount());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
