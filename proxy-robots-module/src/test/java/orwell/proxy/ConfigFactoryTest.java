package orwell.proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.ConfigFactory;
import orwell.proxy.config.EnumConfigFileType;

import static org.junit.Assert.*;

/**
 * Tests for {@link ConfigFactory}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigFactoryTest {

    final static Logger logback = LoggerFactory.getLogger(ConfigFactoryTest.class);
    private static final String CONFIGURATION_FILE_TEST = "/configurationTest.xml";
    private ConfigFactory configFactory;

    @Before
    public void setUp() throws Exception {
        final ConfigCli configCliResource = new ConfigCli(CONFIGURATION_FILE_TEST, EnumConfigFileType.RESOURCE);
        configFactory = new ConfigFactory(configCliResource);
    }

    @Test
    public void testGetConfigProxy() throws Exception {
        logback.debug("IN");
        // Simple test to check the class is well populated
        assertEquals("Receiver linger from configuration.xml should be 10000",
                1000, configFactory.getConfigProxy().getReceiverLinger());
        logback.debug("OUT");
    }

    @Test
    public void testGetConfigRobots() throws Exception {
        logback.debug("IN");
        // Simple test to check the class is well populated
        assertTrue("configuration.xml should contain at least one robot to register",
                !configFactory.getConfigRobots().getConfigRobotsToRegister().isEmpty());
        logback.debug("OUT");
    }

    @Test
    public void testGetConfigServerGame() throws Exception {
        logback.debug("IN");
        // Simple test to check the class is well populated
        assertEquals("configuration.xml should have 'platypus' as priority server game",
                "platypus", configFactory.getConfigServerGame().getName());
        logback.debug("OUT");
    }

    @After
    public void tearDown() throws Exception {


    }
}
