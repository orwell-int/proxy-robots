package orwell.proxy.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import orwell.proxy.ConfigModel;
import orwell.proxy.ConfigProxy;
import orwell.proxy.ConfigServerGame;
import orwell.proxy.Configuration;

/**
 * Tests for {@link ConfigModel}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigurationTest {
	
	String CONFIGURATION_FILE_TEST = "orwell/proxy/test/configurationTest.xml";

	private Configuration buildConfigTest()
	{
    	Configuration configTest = new Configuration(CONFIGURATION_FILE_TEST);
    	try {
			configTest.populate();
		} catch (FileNotFoundException e) {
			fail(e.toString());
		} catch (JAXBException e) {
			fail(e.toString());
		}
    	return configTest;
	}
	
    @Test
    public void populateConfigModel() {
    	assertTrue(buildConfigTest().isPopulated);
    }
    
    @Test
    public void checkProxyList() {
    	
    	ConfigProxy configProxy = buildConfigTest().getConfigModel().getConfigProxy();
    	
    	assertEquals(3, configProxy.getConfigServerGames().size());
    	try {
			assertNotNull(configProxy.getConfigServerGame("platypus"));
		} catch (Exception e) {
			fail(e.toString());
		}
    	
    	assertEquals("localhost", configProxy.getConfigServerGames().get(2).getName());
    }
    
    @Test
    public void checkServerGameElement() {
    	
    	ConfigServerGame configServerGame;
		try {
			configServerGame = buildConfigTest().getConfigModel().getConfigProxy().getConfigServerGame("platypus");
	    	assertEquals("192.168.1.46", configServerGame.getIp());
	    	assertEquals(9000, configServerGame.getPushPort());
	    	assertEquals(9001, configServerGame.getSubPort());
		} catch (Exception e) {
			fail(e.toString());
		}
    }
    
}

