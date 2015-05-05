package orwell.proxy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.*;

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

    final static Logger logback = LoggerFactory.getLogger(ConfigurationTest.class);
    private static final String CONFIGURATION_FILE_TEST = "/configurationTest.xml";
    private static final String CONFIGURATION_URL_TEST = "https://github.com/orwell-int/proxy-robots/blob/master/proxy-robots-module/src/main/resources/configuration.xml";

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private ConfigCli configCli;

    private Configuration buildConfigTest(String fileName, EnumConfigFileType configFileType) {
        configCli = new ConfigCli(fileName, configFileType);
        Configuration configTest = new Configuration(configCli);
        return configTest;
    }

    @Before
    public void setUp() {

        assertNotNull("Test file missing", getClass().getResource(CONFIGURATION_FILE_TEST));
    }


    @Test
    public void testPopulateConfigModel() {


        assertTrue(buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).isPopulated);

    }

    @Test
    public void testProxyList() {

        ConfigProxy configProxy = null;

        configProxy = buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigProxy();


        assertEquals(3, configProxy.getConfigServerGames().size());
        try {
            assertNotNull(configProxy.getConfigServerGame());
        } catch (Exception e) {
            logback.error(e.getMessage());
        }


        assertEquals("localhost", configProxy.getConfigServerGames().get(2)
                .getName());
    }

    @Test
    public void testServerGameElement() {

        ConfigServerGame configServerGame;
        try {
            configServerGame = buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                    .getConfigProxy().getConfigServerGame();
            assertEquals("192.168.1.46", configServerGame.getIp());
            assertEquals(9000, configServerGame.getPushPort());
            assertEquals(9001, configServerGame.getSubPort());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testCommonElements() {

        ConfigProxy configProxy = null;
        configProxy = buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigProxy();


        assertEquals(1000, configProxy.getSenderLinger());
        assertEquals(1000, configProxy.getReceiverLinger());
    }

    @Test
    public void testRobotsList() {

        ConfigRobots configRobots = null;
        configRobots = buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
                .getConfigRobots();


        assertEquals(2, configRobots.getConfigTanks().size());
        try {
            assertNotNull(configRobots.getConfigTank("BananaOne"));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testTankElement() {

        ConfigTank configTank;
        try {
            configTank = buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).getConfigModel().getConfigRobots()
                    .getConfigTank("BananaOne");
            assertEquals("001653119482", configTank.getBluetoothID());
            assertEquals("Daneel", configTank.getBluetoothName());
            assertNotNull(configTank.getConfigCamera());
            assertEquals("192.168.1.50", configTank.getConfigCamera().getIp());
            assertEquals(9100, configTank.getConfigCamera().getPort());
            assertEquals("Yellow hull -- TO BE BETTER CONFIGURED", configTank.getImage());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testRobotsToRegister() {

        ConfigRobots configRobots = null;
        configRobots = buildConfigTest(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE).getConfigModel()
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
        } catch (IOException e) {
            logback.error(e.getMessage());
        }

        // Population fails since file is empty
        assertFalse(buildConfigTest(file.getAbsolutePath(), EnumConfigFileType.FILE).isPopulated);

        logback.debug("OUT");
    }


    @Test
    public void testConfigurationFailsWithURL() {
        logback.debug("IN");

        assertFalse(buildConfigTest(CONFIGURATION_URL_TEST, EnumConfigFileType.URL).isPopulated);

        logback.debug("OUT");
    }
}
