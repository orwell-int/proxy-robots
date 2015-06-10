package orwell.proxy.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.source.ConfigurationResource;

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
    private static final String RESOURCE_TEST_PATH = "/configurationTest.xml";
    private ConfigFactory configFactory;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        final ConfigurationResource resource = new ConfigurationResource(RESOURCE_TEST_PATH);
        configFactory = ConfigFactory.createConfigFactory(resource);
    }

    @Test
    public void testGetConfigProxy() throws Exception {
        // Simple test to check the class is well populated
        assertEquals("Receiver linger from config.xml should be 1000",
                1000, configFactory.getConfigProxy().getReceiverLinger());
    }

    @Test
    public void testGetConfigRobots() throws Exception {
        // Simple test to check the class is well populated
        assertTrue("config.xml should contain at least one robot to register",
                !configFactory.getConfigRobots().getConfigRobotsToRegister().isEmpty());
    }

    @Test
    public void testGetConfigServerGame() throws Exception {
        // Simple test to check the class is well populated
        assertEquals("config.xml should have 'localhost' as priority server game",
                "localhost", configFactory.getMaxPriorityConfigServerGame().getName());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
