package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.mock.MockedCamera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Michaël Ludmann on 11/04/15.
 */
@RunWith(JUnit4.class)
public class LegoTankTest {
    private final static Logger logback = LoggerFactory.getLogger(LegoTankTest.class);

    @TestSubject
    private LegoTank tank;

    @Before
    public void setUp() {
        logback.info("IN");
        tank = new LegoTank("", "", new MockedCamera(), "");
        logback.info("OUT");
    }

    @Test
    public void testToString() throws Exception {
        final String tankString = "Tank {[BTName]  [BT-ID]  [RoutingID]  [TeamName] }";
        assertEquals(tankString, tank.toString());


    }

    @Test
    /**
     * 1. Tank reads an RFID value: first read of ServerRobotState is not null
     * 2. Nothing happens: second read of ServerRobotState is null
     * 3. Tank reads a Color value: third read of ServerRobotState is not null
     */
    public void testReceivedNewMessage() {
        logback.info("IN");

        UnitMessage unitMessage = new UnitMessage(UnitMessageType.Rfid, "123");
        tank.receivedNewMessage(unitMessage);

        final RobotElementStateVisitor stateVisitor = new RobotElementStateVisitor();
        tank.accept(stateVisitor);

        assertNotNull(stateVisitor.getServerRobotStateBytes());

        stateVisitor.reset();
        tank.accept(stateVisitor);
        assertNull(stateVisitor.getServerRobotStateBytes());

        unitMessage = new UnitMessage(UnitMessageType.Colour, "2");
        tank.receivedNewMessage(unitMessage);

        stateVisitor.reset();
        tank.accept(stateVisitor);
        assertNotNull(stateVisitor.getServerRobotStateBytes());

        logback.info("OUT");
    }

}
