package orwell.proxy.robot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import orwell.proxy.config.ConfigCamera;
import orwell.proxy.config.ConfigTank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
@RunWith(JUnit4.class)
public class RobotFactoryTest {

    private static final String BT_NAME_TEST = "BtNameTest";
    private static final String BT_ID_TEST = "BtIdTest";
    private static final String IMAGE_TEST = "ImageTest";
    private static final String TEMP_ROUTING_ID_TEST = "TempRoutingIdTest";
    private static final String IP_TEST = "IpTest";
    private static final int PORT_TEST = 777;
    private RobotFactory robotFactory;

    @Before
    public void setUp() {
        robotFactory = new RobotFactory();
    }

    @Test
    public void testGetLegoTank_NullConfigCamera() throws Exception {
        final LegoTank legoTank = (LegoTank) robotFactory.getRobot(new ConfigTank());
        assertEquals(IPWebcam.getDummy().getUrl(), legoTank.getCameraUrl());
    }

    @Test
    public void testGetLegoTank_NullParameter() throws Exception {
        final LegoTank legoTank = (LegoTank) robotFactory.getRobot(null);
        assertNull(legoTank);
    }

    @Test
    public void testGetLegoTank_normalConfig() throws Exception {
        // Manually setup the configuration of tank
        final ConfigTank configTank = new ConfigTank();
        configTank.setBluetoothID(BT_ID_TEST);
        configTank.setBluetoothName(BT_NAME_TEST);
        configTank.setImage(IMAGE_TEST);
        configTank.setShouldRegister(true);
        configTank.setTempRoutingID(TEMP_ROUTING_ID_TEST);

        // idem for camera of tank
        final ConfigCamera configCamera = new ConfigCamera();
        configCamera.setIp(IP_TEST);
        configCamera.setPort(PORT_TEST);
        configTank.setCamera(configCamera);

        // Build a tank from the config
        final LegoTank legoTank = (LegoTank) robotFactory.getRobot(configTank);
        assertEquals(IMAGE_TEST, legoTank.getImage());
        assertEquals("http://" + IP_TEST + ":" + PORT_TEST, legoTank.getCameraUrl());
    }
}
