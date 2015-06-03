package orwell.proxy.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ConfigFactory}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigFactoryTest {

    private final static Logger logback = LoggerFactory.getLogger(ConfigFactoryTest.class);
    private static final String CONFIGURATION_RESOURCE_TEST = "/configurationTest.xml";
    private ConfigFactory configFactory;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        final ConfigFactoryParameters configFactoryParametersResource = new ConfigFactoryParameters(CONFIGURATION_RESOURCE_TEST, EnumConfigFileType.RESOURCE);
        configFactory = ConfigFactory.createConfigFactory(configFactoryParametersResource);
    }

    @Test
    public void testGetConfigProxy() throws Exception {
        // Simple test to check the class is well populated
        assertEquals("Receiver linger from configuration.xml should be 1000",
                1000, configFactory.getConfigProxy().getReceiverLinger());
    }

    @Test
    public void testGetConfigRobots() throws Exception {
        // Simple test to check the class is well populated
        assertTrue("configuration.xml should contain at least one robot to register",
                !configFactory.getConfigRobots().getConfigRobotsToRegister().isEmpty());
    }

    @Test
    public void testGetConfigServerGame() throws Exception {
        // Simple test to check the class is well populated
        assertEquals("configuration.xml should have 'localhost' as priority server game",
                "localhost", configFactory.getMaxPriorityConfigServerGame().getName());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
