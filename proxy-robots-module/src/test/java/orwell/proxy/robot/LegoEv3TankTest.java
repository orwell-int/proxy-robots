package orwell.proxy.robot;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Michaël Ludmann on 03/07/16.
 */
public class LegoEv3TankTest {
    private final String IP_ADDRESS = "192.168.0.17";
    private final String MAC_ADDRESS = "00:11:22:AA:BB";
    private final int VIDEO_STREAM_PORT = 1111;
    private final String IMAGE = "YELLOW HULL";
    private final int PUSH_PORT = 10000;
    private final int PULL_PORT = 10001;
    private final String HOSTNAME = "HOSTNAME_TEST";
    private LegoEv3Tank tank;

    @Before
    public void setUp() {
        // Instantiate default tank
        tank = new LegoEv3Tank(IP_ADDRESS, MAC_ADDRESS,
                VIDEO_STREAM_PORT, IMAGE,
                PUSH_PORT, PULL_PORT, HOSTNAME);
    }

    @Test
    public void getCamera() {
        assertEquals("nc:" + IP_ADDRESS + ":" + VIDEO_STREAM_PORT, tank.getCameraUrl());
    }

}