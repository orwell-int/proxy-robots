package orwell.proxy.robot;

import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;
import orwell.proxy.mock.MockedCamera;

import static org.junit.Assert.assertEquals;


/**
 * Created by Michaël Ludmann on 11/04/15.
 */
@RunWith(JUnit4.class)
public class RobotElementStateVisitorTest {
    final static Logger logback = LoggerFactory.getLogger(RobotElementStateVisitorTest.class);

    @Mock
    private final LegoTank tank = new LegoTank("", "", new MockedCamera(), "");

    @TestSubject
    private RobotElementStateVisitor stateVisitor;


    @Before
    public void setUp() {

        logback.info("IN");
        stateVisitor = new RobotElementStateVisitor();
        logback.info("OUT");
    }


    @Test
    public void testVisit_rfid() {

        tank.setRfidValue("2");
        tank.accept(stateVisitor);

        assertEquals("2", stateVisitor.getServerRobotState().getRfid(0).getRfid());
        assertEquals(Robot.Status.ON, stateVisitor.getServerRobotState().getRfid(0).getStatus());
    }


    @Test
    public void testVisit_colour() {

        tank.setColourValue("7");
        tank.accept(stateVisitor);

        assertEquals(7, stateVisitor.getServerRobotState().getColour(0).getColour());
        assertEquals(Robot.Status.ON, stateVisitor.getServerRobotState().getColour(0).getStatus());
    }


    @After
    public void tearDown() {

    }
}
