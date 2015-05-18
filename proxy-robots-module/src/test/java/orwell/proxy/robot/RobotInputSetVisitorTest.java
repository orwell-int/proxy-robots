package orwell.proxy.robot;

import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;
import orwell.proxy.ProtobufTest;
import orwell.proxy.mock.MockedCamera;

import static org.junit.Assert.assertEquals;


/**
 * Created by Michaël Ludmann on 11/04/15.
 */
@RunWith(JUnit4.class)
public class RobotInputSetVisitorTest {
    final static Logger logback = LoggerFactory.getLogger(RobotInputSetVisitorTest.class);
    private final Tank2 tank = new Tank2(new MockedCamera());

    @TestSubject
    private RobotInputSetVisitor inputSetVisitor;


    @Before
    public void setUp() {

        logback.info("IN");
        inputSetVisitor = new RobotInputSetVisitor(ProtobufTest.buildTestInput().toByteArray());
        logback.info("OUT");
    }


    @Test
    public void testVisit_RobotMove() {

        tank.accept(inputSetVisitor);

    }


    @Test
    public void testVisit_RobotFire() {

        tank.accept(inputSetVisitor);
    }


    @After
    public void tearDown() {

    }
}
