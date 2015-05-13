package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;
import orwell.proxy.mock.MockedCamera;

import static org.junit.Assert.*;


/**
 * Created by Michaël Ludmann on 11/04/15.
 */
@RunWith(JUnit4.class)
public class TankTest {
    final static Logger logback = LoggerFactory.getLogger(TankTest.class);

    @TestSubject
    private Tank tank;

    @Before
    public void setUp() {
        logback.info("IN");
        tank = new Tank(new MockedCamera());
        logback.info("OUT");
    }

    @Test
    public void testPrintVisitor() {
        tank.accept(new RobotElementPrintVisitor());
    }

    @Test
    public void testStateVisitor() {
        tank.setRfidValue("2");
        final RobotElementStateVisitor stateVisitor = new RobotElementStateVisitor();
        tank.accept(stateVisitor);
        assertEquals("2", stateVisitor.getServerRobotState().getRfid(0).getRfid());
        assertEquals(Robot.Status.ON, stateVisitor.getServerRobotState().getRfid(0).getStatus());


    }

//    @Before
//    public void setUp() {
//        logback.info("IN");
//        tank = new Tank("BtName", "BtId", new MockedCamera(), "Image");
//        logback.info("OUT");
//    }
//
//    @Test
//    /**
//     * 1. Tank reads an RFID value: first read of ServerRobotState is not null
//     * 2. Nothing happens: second read of ServerRobotState is null
//     * 3. Tank reads a Color value: third read of ServerRobotState is not null
//     */
//    public void testGetAndClearZmqServerRobotState() {
//        logback.info("IN");
//
//        UnitMessage unitMessage = new UnitMessage(UnitMessageType.Rfid, "123");
//        tank.receivedNewMessage(unitMessage);
//
//        assertNotNull(tank.getServerRobotStateBytes_And_ClearDelta());
//
//        assertNull(tank.getServerRobotStateBytes_And_ClearDelta());
//
//        unitMessage = new UnitMessage(UnitMessageType.Colour, "2");
//        tank.receivedNewMessage(unitMessage);
//
//        assertNotNull(tank.getServerRobotStateBytes_And_ClearDelta());
//
//        logback.info("OUT");
//    }

    @After
    public void tearDown() {

    }
}
