package orwell.proxy.robot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.elements.ConfigCamera;
import orwell.proxy.config.elements.ConfigNetworkInterface;
import orwell.proxy.config.elements.ConfigRobotException;
import orwell.proxy.config.elements.ConfigTank;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
@RunWith(JUnit4.class)
public class RobotFactoryTest {

    private final static Logger logback = LoggerFactory.getLogger(RobotFactoryTest.class);
    private static final String BT_NAME_TEST = "BtNameTest";
    private static final String BT_ID_TEST = "BtIdTest";
    private static final String IMAGE_TEST = "ImageTest";
    private static final String TEMP_ROUTING_ID_TEST = "TempRoutingIdTest";
    private static final String IP_TEST = "IpTest";
    private static final String MODEL_EV3_TEST = "ev3";
    private static final String MODEL_NXT_TEST = "nxt";
    private static final int PORT_TEST = 777;
    private static final String NETWORK_ADDR_TEST = "wlan0";
    private static final String MAC_TEST = "00:11:22:AA:BB";
    private RobotFactory robotFactory;

    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        robotFactory = new RobotFactory();
    }

    @Test
    public void testGetLegoTank_NullConfigCamera() throws Exception {
        final LegoNxtTank legoNxtTank = (LegoNxtTank) robotFactory.getRobot(new ConfigTank());
        assertEquals(IPWebcam.getDummy().getUrl(), legoNxtTank.getCameraUrl());
    }

    @Test
    public void testGetLegoTank_NullParameter() throws Exception {
        final LegoNxtTank legoNxtTank = (LegoNxtTank) robotFactory.getRobot(null);
        assertNull(legoNxtTank);
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
        final LegoNxtTank legoNxtTank = (LegoNxtTank) robotFactory.getRobot(configTank);
        assertEquals(IMAGE_TEST, legoNxtTank.getImage());
        assertEquals("http://" + IP_TEST + ":" + PORT_TEST, legoNxtTank.getCameraUrl());
    }

    @Test
    public void getRobotLegoEv3Tank() throws ConfigRobotException {
        final ConfigNetworkInterface cni = new ConfigNetworkInterface();
        cni.setNetworkAddress(NETWORK_ADDR_TEST);
        cni.setIpAddress(IP_TEST);
        cni.setMacAddress(MAC_TEST);

        // idem for camera of tank
        final ConfigCamera configCamera = new ConfigCamera();
        configCamera.setPort(PORT_TEST);

        final ConfigTank configTank = new ConfigTank();
        configTank.setConfigNetworkInterfaces(Arrays.asList(cni));
        configTank.setCamera(configCamera);
        configTank.setModel(MODEL_EV3_TEST);
        configTank.setImage(IMAGE_TEST);

        final IRobot robot = robotFactory.getRobot(configTank);

        assertEquals(LegoEv3Tank.class, robot.getClass());
    }

    @Test
    public void getRobotLegoEv3Tank_BadNetworkAddr() throws ConfigRobotException {
        final ConfigNetworkInterface cni = new ConfigNetworkInterface();
        cni.setNetworkAddress("");

        final ConfigTank configTank = new ConfigTank();
        configTank.setConfigNetworkInterfaces(Arrays.asList(cni));
        configTank.setModel("ev3");

        final IRobot robot = robotFactory.getRobot(configTank);

        assertNull(robot);
    }

    @Test
    public void getRobotLegoNxtTank() throws ConfigRobotException {
        final ConfigTank configTank = new ConfigTank();
        configTank.setModel(MODEL_NXT_TEST);

        final IRobot robot = robotFactory.getRobot(configTank);

        assertEquals(LegoNxtTank.class, robot.getClass());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
