package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.ProtobufTest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Michaël Ludmann on 6/11/15.
 */
@RunWith(JUnit4.class)
public class InputMoveTest {
    private final static Logger logback = LoggerFactory.getLogger(InputMoveTest.class);
    private static final String INPUT_MOVE = "move 50.5 10.0";
    private InputMove inputMove;

    @Before
    public void setUp() throws Exception {
        logback.debug(">>>>>>>>> IN");
        inputMove = new InputMove();
    }

    @Test
    public void testSetMove() throws Exception {
        inputMove.setMove(ProtobufTest.getTestInput().getMove());
        assertTrue(inputMove.hasMove());
    }

    @Test
    public void testSendUnitMessageTo() throws Exception {
        inputMove.setMove(ProtobufTest.getTestInput().getMove());

        final LegoTank legoTank = createNiceMock(LegoTank.class);
        final Capture<UnitMessage> messageCapture = new Capture<>();
        legoTank.sendUnitMessage(capture(messageCapture));
        expectLastCall().once();
        replay(legoTank);

        inputMove.sendUnitMessageTo(legoTank);
        verify(legoTank);
        assertEquals(UnitMessageType.Command, messageCapture.getValue().getMsgType());
        assertEquals(INPUT_MOVE, messageCapture.getValue().getPayload());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}

