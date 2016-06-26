package orwell.proxy.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import orwell.proxy.config.elements.ConfigNetworkInterface;
import orwell.proxy.config.elements.ConfigTank;
import orwell.proxy.config.source.ConfigurationResource;
import orwell.proxy.robot.EnumModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Michaël Ludmann on 23/06/16.
 */

@RunWith(JUnit4.class)
public class ConfigurationEv3Test {

    private static final String CONF_WIFI_RESOURCE_PATH = "/configurationEv3Test.xml";

    @Test
    public void testGetTankEnumModelEv3() {
        final ConfigTank configTank;
        try {
            configTank = (ConfigTank) new ConfigurationResource(CONF_WIFI_RESOURCE_PATH).getConfigModel().getConfigRobots()
                    .getConfigRobot("BananaWifiOne");
            assertEquals(EnumModel.EV3, configTank.getEnumModel());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetConfigNetworkInterfaces() {
        final ConfigTank configTank;
        try {
            configTank = (ConfigTank) new ConfigurationResource(CONF_WIFI_RESOURCE_PATH).getConfigModel().getConfigRobots()
                    .getConfigRobot("BananaWifiOne");
            assertEquals(2, configTank.getConfigNetworkInterfaces().size());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetWlan0Elements() {
        final ConfigTank configTank;
        final ConfigNetworkInterface configNetworkInterfaceWlan0;
        try {
            configTank = (ConfigTank) new ConfigurationResource(CONF_WIFI_RESOURCE_PATH).getConfigModel().getConfigRobots()
                    .getConfigRobot("BananaWifiOne");
            configNetworkInterfaceWlan0 = configTank.getConfigNetworkInterface("wlan0");
            assertEquals("192.168.2.219", configNetworkInterfaceWlan0.getIpAddress());
            assertEquals("74:DA:38:7F:89:61", configNetworkInterfaceWlan0.getMacAddress());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetWBr0Elements() {
        final ConfigTank configTank;
        final ConfigNetworkInterface configNetworkInterfaceBr0;
        try {
            configTank = (ConfigTank) new ConfigurationResource(CONF_WIFI_RESOURCE_PATH).getConfigModel().getConfigRobots()
                    .getConfigRobot("BananaWifiOne");
            configNetworkInterfaceBr0 = configTank.getConfigNetworkInterface("br0");
            assertEquals("10.0.1.1", configNetworkInterfaceBr0.getIpAddress());
            assertEquals("02:16:53:42:C3:AA", configNetworkInterfaceBr0.getMacAddress());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }
}
