package orwell.proxy.robot;

import junit.framework.AssertionFailedError;
import lejos.mf.common.UnitMessage;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.ProtobufTest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Michaël Ludmann on 11/04/15.
 */
@RunWith(JUnit4.class)
public class RobotInputSetVisitorTest {
    private final static Logger logback = LoggerFactory.getLogger(RobotInputSetVisitorTest.class);

    @TestSubject
    private RobotInputSetVisitor inputSetVisitor;

    @Mock
    private LegoTank tank;


    @Before
    public void setUp() {

        logback.info("IN");
        inputSetVisitor = new RobotInputSetVisitor(ProtobufTest.getTestInput().toByteArray());
        logback.info("OUT");
    }


    @Test
    public void testVisit_RobotMove() {

        final InputMove inputMove = new InputMove();
        assertFalse(inputMove.hasMove());

        inputSetVisitor.visit(inputMove);
        assertTrue(inputMove.hasMove());
    }


    @Test
    public void testVisit_RobotFire() {

        final InputFire inputFire = new InputFire();
        assertFalse(inputFire.hasFire());

        inputSetVisitor.visit(inputFire);
        assertTrue(inputFire.hasFire());
    }


    @Test
    public void testVisit_Robot_Empty() {

        // Mock the tank
        tank = createMock(LegoTank.class);
        tank.sendUnitMessage(anyObject(UnitMessage.class));
        // We should not send any unitMessage (or we throw an exception)
        expectLastCall().andThrow(new AssertionFailedError("Tank should not send an unitMessage")).anyTimes();
        replay(tank);

        // Perform the actual visit
        inputSetVisitor.visit(tank);

        verify(tank);
    }


    @Test
    public void testVisit_Robot_Full() {

        // Setup the class
        final InputMove inputMove = new InputMove();
        inputSetVisitor.visit(inputMove);
        final InputFire inputFire = new InputFire();
        inputSetVisitor.visit(inputFire);

        // Mock the tank
        tank = createMock(LegoTank.class);
        tank.sendUnitMessage(anyObject(UnitMessage.class));
        expectLastCall().times(2); // we should send two unitMessages
        replay(tank);

        // Perform the actual visit
        inputSetVisitor.visit(tank);

        verify(tank);
    }


}
