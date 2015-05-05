package orwell.proxy;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot.ServerRobotState;
import orwell.messages.Robot.Status;

import static org.junit.Assert.assertEquals;

/**
 * Created by parapampa on 01/02/15.
 */
public class TankLatestStatesTest {
    final static Logger logback = LoggerFactory.getLogger(TankLatestStatesTest.class);

    @TestSubject
    private TankDeltaState tankDeltaState;

    @Before
    public void setUp() {
        logback.info("IN");
        tankDeltaState = new TankDeltaState();
        logback.info("OUT");
    }

    @Test
    public void testSetNewRfid() {
        logback.info("IN");

        tankDeltaState.setNewState(EnumSensor.RFID, "1234567890");

        ServerRobotState serverRobotState = tankDeltaState.getServerRobotState();
        assertEquals(1, serverRobotState.getRfidCount());
        assertEquals("1234567890", serverRobotState.getRfid(0).getRfid());
        assertEquals(Status.ON, serverRobotState.getRfid(0).getStatus());

        logback.info("OUT");
    }

    @Test
    public void testSetSecondNewRfid() {
        logback.info("IN");

        tankDeltaState.setNewState(EnumSensor.RFID, "1234567890");
        tankDeltaState.setNewState(EnumSensor.RFID, "1234567891");

        ServerRobotState serverRobotState = tankDeltaState.getServerRobotState();
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

        tankDeltaState.setNewState(EnumSensor.RFID, "1234567890");

        ServerRobotState serverRobotState = tankDeltaState.getAndClearServerRobotState();
        assertEquals(1, serverRobotState.getRfidCount());
        ServerRobotState clearedServerRobotState = tankDeltaState.getServerRobotState();
        assertEquals(0, clearedServerRobotState.getRfidCount());

        logback.info("OUT");
    }

    @Test
    /**
     * Test the status stays to ON when the tank set twice in a row the same RFID value
     */
    public void testSetSameRfid() {
        logback.info("IN");

        tankDeltaState.setNewState(EnumSensor.RFID, "123456789");
        tankDeltaState.setNewState(EnumSensor.RFID, "123456789");

        ServerRobotState serverRobotState = tankDeltaState.getServerRobotState();
        assertEquals(1, serverRobotState.getRfidCount());
        assertEquals("123456789", serverRobotState.getRfid(0).getRfid());
        assertEquals(Status.ON, serverRobotState.getRfid(0).getStatus());

        logback.info("OUT");
    }
}
