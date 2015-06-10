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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

/**
 * Tests for {@link ConfigModel}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigurationTest {

    private final static Logger logback = LoggerFactory.getLogger(ConfigurationTest.class);
    private static final String CONFIGURATION_RESOURCE_PATH = "/configurationTest.xml";
    private static final int PUSH_PORT = 9001;
    private static final int SUB_PORT = 9000;
    private static final int CAMERA_PORT_TANK = 9100;
    private static final int CAMERA_PORT_SCOUT = 9102;
    private static final int LINGER_TIME_MS = 1000;
    private static final int OUTGOING_MSG_FREQ_MS = 500;
    private static final int UDP_BROADCAST_TIMEOUT = 1000;
    private static final int UDP_BROADCAST_ATTEMPTS = 5;
    private static final int UDP_BROADCAST_PORT = 9080;
    private static final int RECEIVE_TIMEOUT_MS = 300000;
    private static final String SERVER_ADDRESS = "tcp://127.0.0.1:";

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

        assertEquals(RECEIVE_TIMEOUT_MS, configProxy.getReceiveTimeout());
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
    public void testConfigurationFile_fail() throws FileNotFoundException {
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
    public void testUdpBroadcastElement() {
        final ConfigUdpBroadcast configUdpBroadcast;
        try {
            configUdpBroadcast = getConfigurationResourceTest().getConfigModel()
                    .getConfigProxy().getConfigUdpBroadcast();
            assertEquals(UDP_BROADCAST_PORT, configUdpBroadcast.getPort());
            assertEquals(UDP_BROADCAST_ATTEMPTS, configUdpBroadcast.getAttempts());
            assertEquals(UDP_BROADCAST_TIMEOUT, configUdpBroadcast.getTimeoutPerAttemptMs());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testConfiguration_loadsDefaultResource() {
        assertTrue(new ConfigurationResource("configThatDoesNotExist.xml").isPopulated());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
