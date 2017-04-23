package orwell.proxy.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import orwell.proxy.config.elements.ConfigNetworkInterface;
import orwell.proxy.config.elements.ConfigRobotException;
import orwell.proxy.config.elements.ConfigTank;
import orwell.proxy.config.source.ConfigurationResource;
import orwell.proxy.robot.EnumModel;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ConfigurationEv3Test {

    private static final String CONF_WIFI_RESOURCE_PATH = "/configurationEv3Test.xml";
    private ConfigTank configTank;

    @Before
    public void setup() throws Exception {
        configTank = (ConfigTank) new ConfigurationResource(CONF_WIFI_RESOURCE_PATH).getConfigModel().getConfigRobots()
                .getConfigRobot("BananaWifiOne");
    }

    @Test
    public void testGetTankEnumModelEv3() {
        assertEquals(EnumModel.EV3, configTank.getEnumModel());
    }

    @Test
    public void testGetConfigNetworkInterfaces() {
        assertEquals(2, configTank.getConfigNetworkInterfaces().size());
    }

    @Test
    public void testGetWlan0Elements() throws ConfigRobotException {
        final ConfigNetworkInterface configNetworkInterfaceWlan0;
        configNetworkInterfaceWlan0 = configTank.getConfigNetworkInterface("wlan0");
        assertEquals("192.168.2.219", configNetworkInterfaceWlan0.getIpAddress());
    }

    @Test
    public void testGetWBr0Elements() throws ConfigRobotException {
        final ConfigNetworkInterface configNetworkInterfaceBr0;
        configNetworkInterfaceBr0 = configTank.getConfigNetworkInterface("br0");
        assertEquals("10.0.1.1", configNetworkInterfaceBr0.getIpAddress());
    }

    @Test
    public void testGetHostname() {
        assertEquals("R2D2", configTank.getHostname());
    }
}
