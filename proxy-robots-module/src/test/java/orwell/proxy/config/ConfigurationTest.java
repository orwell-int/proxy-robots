package orwell.proxy.config;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for {@link ConfigModel}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigurationTest {

    private final static Logger logback = LoggerFactory.getLogger(ConfigurationTest.class);
    private static final String CONFIGURATION_RESOURCE_TEST = "/configurationTest.xml";
    private static final String CONFIGURATION_URL_TEST = "https://github.com/orwell-int/proxy-robots/blob/master/proxy-robots-module/src/main/resources/configuration.xml";
    private static final int PUSH_PORT = 9000;
    private static final int SUB_PORT = 9001;
    private static final int CAMERA_PORT = 9100;
    private static final int LINGER_TIME_MS = 1000;
    private static final int OUTGOING_MSG_FREQ_MS = 500;



    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Configuration getConfigTest(final String fileName, final EnumConfigFileType configFileType) {
        final ConfigFactoryParameters configFactoryParameters = new ConfigFactoryParameters(fileName, configFileType);
        return new Configuration(configFactoryParameters);
    }

    @Before
    public void setUp() {

        assertNotNull("Test file missing", getClass().getResource(CONFIGURATION_RESOURCE_TEST));
    }


    @Test
    public void testPopulateConfigModel() {

        assertTrue(getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).isPopulated());
    }

    @Test
    public void testProxyList() {

        final ConfigProxy configProxy;
        configProxy = getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigProxy();


        assertEquals(3, configProxy.getConfigServerGames().size());
        try {
            assertNotNull(configProxy.getConfigServerGame());
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
            configServerGame = getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                    .getConfigProxy().getConfigServerGame();
            assertEquals("192.168.1.46", configServerGame.getIp());
            assertEquals(PUSH_PORT, configServerGame.getPushPort());
            assertEquals(SUB_PORT, configServerGame.getSubPort());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testConfigProxy() {

        final ConfigProxy configProxy;
        configProxy = getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigProxy();

        assertEquals(LINGER_TIME_MS, configProxy.getSenderLinger());
        assertEquals(LINGER_TIME_MS, configProxy.getReceiverLinger());
        assertEquals(OUTGOING_MSG_FREQ_MS, configProxy.getOutgoingMsgPeriod());
    }

    @Test
    public void testRobotsList() {

        final ConfigRobots configRobots;
        configRobots = getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigRobots();


        assertEquals(2, configRobots.getConfigTanks().size());
        try {
            assertNotNull(configRobots.getConfigTank("BananaOne"));
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testTankElement() {

        final ConfigTank configTank;
        try {
            configTank = getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).getConfigModel().getConfigRobots()
                    .getConfigTank("BananaOne");
            assertEquals("001653119482", configTank.getBluetoothID());
            assertEquals("Daneel", configTank.getBluetoothName());
            assertNotNull(configTank.getConfigCamera());
            assertEquals("192.168.1.50", configTank.getConfigCamera().getIp());
            assertEquals(CAMERA_PORT, configTank.getConfigCamera().getPort());
            assertEquals("/videofeed", configTank.getConfigCamera().getResourcePath());
            assertEquals("Yellow hull -- TO BE BETTER CONFIGURED", configTank.getImage());
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testRobotsToRegister() {

        final ConfigRobots configRobots;
        configRobots = getConfigTest(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigRobots();


        assertEquals(1, configRobots.getConfigRobotsToRegister().size());
    }

    @Test
    public void testConfigurationFailsWithEmptyFile() {
        logback.debug("IN");
        File file = null;
        try {
            // Create empty file
            file = File.createTempFile("testConfigurationWithFile", ".tmp");
        } catch (final IOException e) {
            fail(e.toString());
        }

        // Population fails since file is empty
        assertFalse(getConfigTest(file.getAbsolutePath(), EnumConfigFileType.FILE).isPopulated());

        logback.debug("OUT");
    }


    @Test
    public void testConfigurationFailsWithURL() {
        logback.debug("IN");

        assertFalse(getConfigTest(CONFIGURATION_URL_TEST, EnumConfigFileType.URL).isPopulated());
        assertNull(getConfigTest(CONFIGURATION_URL_TEST, EnumConfigFileType.URL).getConfigModel());

        logback.debug("OUT");
    }
}
