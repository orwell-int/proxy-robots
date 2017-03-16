package orwell.proxy;

import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class RobotsPortsPoolTests {
    public static final int BEGIN_PORT = 10000;
    public static final int PORTS_COUNT = 4;
    private RobotsPortsPool robotsPortsPool;


    @Before
    public void setup() {
        robotsPortsPool = new RobotsPortsPool(BEGIN_PORT, PORTS_COUNT);
    }

    @Test
    public void getAvailablePort() throws Exception {
        getAllAvailablePorts();
    }

    @Test(expected = NoSuchElementException.class)
    public void getAvailablePort_Exception() throws Exception {
        getAllAvailablePorts();

        robotsPortsPool.getAvailablePort();
    }

    @Test(expected = AssertionError.class)
    public void constructor_InvalidBeginPort_Negative() {
        robotsPortsPool = new RobotsPortsPool(-1, PORTS_COUNT);
    }

    @Test(expected = AssertionError.class)
    public void constructor_InvalidBeginPort_TooBig() {
        robotsPortsPool = new RobotsPortsPool(67000, PORTS_COUNT);
    }

    @Test(expected = AssertionError.class)
    public void constructor_InvalidPortsCount_Negative() {
        robotsPortsPool = new RobotsPortsPool(BEGIN_PORT, -1);
    }

    private void getAllAvailablePorts() {
        for (int i = 0; i < PORTS_COUNT; i++) {
            assertEquals(BEGIN_PORT + i, robotsPortsPool.getAvailablePort());
        }
    }

}