package orwell.proxy;

import static org.junit.Assert.assertEquals;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot.ServerRobotState;
import orwell.messages.Robot.Status;

/**
 * Created by parapampa on 01/02/15.
 */
public class TankCurrentStateTest {
    final static Logger logback = LoggerFactory.getLogger(TankCurrentStateTest.class);

    @TestSubject
    private TankCurrentState tankCurrentState;

    @Before
    public void setUp() {
        logback.info("IN");
        tankCurrentState = new TankCurrentState();
        logback.info("OUT");
    }

    @Test
    public void testSetNewRfid() {
        logback.info("IN");

        tankCurrentState.setNewRfid("1234567890");

        ServerRobotState serverRobotState = tankCurrentState.getServerRobotState();
        assertEquals(1, serverRobotState.getRfidCount());
        assertEquals("1234567890", serverRobotState.getRfid(0).getRfid());
        assertEquals(Status.ON, serverRobotState.getRfid(0).getStatus());

        logback.info("OUT");
    }

    @Test
    public void testSetSecondNewRfid() {
        logback.info("IN");

        tankCurrentState.setNewRfid("1234567890");
        tankCurrentState.setNewRfid("1234567891");


        ServerRobotState serverRobotState = tankCurrentState.getServerRobotState();
        assertEquals(3, serverRobotState.getRfidCount());
        assertEquals("1234567890", serverRobotState.getRfid(0).getRfid());
        assertEquals("1234567890", serverRobotState.getRfid(1).getRfid());
        assertEquals(Status.OFF, serverRobotState.getRfid(1).getStatus());
        assertEquals("1234567891", serverRobotState.getRfid(2).getRfid());
        assertEquals(Status.ON, serverRobotState.getRfid(2).getStatus());

        logback.info("OUT");
    }

    @Test
    public void testGetAndClearServerRobotState() {
        logback.info("IN");

        tankCurrentState.setNewRfid("1234567890");

        ServerRobotState serverRobotState = tankCurrentState.getAndClearServerRobotState();
        assertEquals(1, serverRobotState.getRfidCount());
        ServerRobotState clearedServerRobotState = tankCurrentState.getServerRobotState();
        assertEquals(0, clearedServerRobotState.getRfidCount());

        logback.info("OUT");
    }

    @Test
    /**
     * Test we do not register twice in a row the same RFID value
     */
    public void testSetSameRfid() {
        logback.info("IN");

        tankCurrentState.setNewRfid("0");
        tankCurrentState.setNewRfid("0");

        ServerRobotState serverRobotState = tankCurrentState.getServerRobotState();
        assertEquals(1, serverRobotState.getRfidCount());
        assertEquals("0", serverRobotState.getRfid(0).getRfid());
        assertEquals(Status.ON, serverRobotState.getRfid(0).getStatus());

        logback.info("OUT");
    }
}
