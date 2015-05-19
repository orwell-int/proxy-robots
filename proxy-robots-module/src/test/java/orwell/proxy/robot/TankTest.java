package orwell.proxy.robot;

import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.mock.MockedCamera;


/**
 * Created by Michaël Ludmann on 11/04/15.
 */
@RunWith(JUnit4.class)
public class TankTest {
    final static Logger logback = LoggerFactory.getLogger(TankTest.class);

    @TestSubject
    private LegoTank tank;

    @Before
    public void setUp() {
        logback.info("IN");
        tank = new LegoTank("", "", new MockedCamera(), "");
        logback.info("OUT");
    }


    @Test
    public void testStateVisitor() {

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
