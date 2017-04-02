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
    private final static Logger logback = LoggerFactory.getLogger(RobotElementStateVisitorTest.class);

    @Mock
    private final LegoNxtTank tank = new LegoNxtTank("", "", new MockedCamera(), "");

    @TestSubject
    private RobotElementStateVisitor stateVisitor;


    @Before
    public void setUp() {
        logback.debug(">>>>>>>>> IN");
        stateVisitor = new RobotElementStateVisitor();
    }

    @Test
    public void testVisit_colour() {
        tank.setColourValue("7");
        tank.accept(stateVisitor);

        assertEquals(7, stateVisitor.getServerRobotState().getColour(0).getColour());
        assertEquals(Robot.Status.ON, stateVisitor.getServerRobotState().getColour(0).getStatus());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}
