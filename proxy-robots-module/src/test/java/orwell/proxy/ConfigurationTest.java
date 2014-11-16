package orwell.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.ConfigProxy;
import orwell.proxy.config.ConfigRobots;
import orwell.proxy.config.ConfigServerGame;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.Configuration;

/**
 * Tests for {@link ConfigModel}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ConfigurationTest {

	private static final String CONFIGURATION_FILE_TEST = "src/test/resources/configurationTest.xml";

	private Configuration buildConfigTest() {
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
	public void testPopulateConfigModel() {

		assertTrue(buildConfigTest().isPopulated);
	}

	@Test
	public void testProxyList() {

		ConfigProxy configProxy = buildConfigTest().getConfigModel()
				.getConfigProxy();

		assertEquals(3, configProxy.getConfigServerGames().size());
		try {
			assertNotNull(configProxy.getConfigServerGame("platypus"));
		} catch (Exception e) {
			fail(e.toString());
		}

		assertEquals("localhost", configProxy.getConfigServerGames().get(2)
				.getName());
	}

	@Test
	public void testServerGameElement() {

		ConfigServerGame configServerGame;
		try {
			configServerGame = buildConfigTest().getConfigModel()
					.getConfigProxy().getConfigServerGame("platypus");
			assertEquals("192.168.1.46", configServerGame.getIp());
			assertEquals(9000, configServerGame.getPushPort());
			assertEquals(9001, configServerGame.getSubPort());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void testCommonElements() {

		ConfigProxy configProxy = buildConfigTest().getConfigModel()
				.getConfigProxy();

		assertEquals(1000, configProxy.getSenderLinger());
		assertEquals(1000, configProxy.getReceiverLinger());
	}

	@Test
	public void testRobotsList() {

		ConfigRobots configRobots = buildConfigTest().getConfigModel()
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
			configTank = buildConfigTest().getConfigModel().getConfigRobots()
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

		ConfigRobots configRobots = buildConfigTest().getConfigModel()
				.getConfigRobots();

		assertEquals(1, configRobots.getConfigRobotsToRegister().size());
	}
}
