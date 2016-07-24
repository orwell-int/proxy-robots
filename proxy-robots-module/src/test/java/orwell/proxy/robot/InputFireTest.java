package orwell.proxy.robot;

import lejos.mf.common.StreamUnitMessage;
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
public class InputFireTest {
    private final static Logger logback = LoggerFactory.getLogger(InputFireTest.class);
    private static final String INPUT_FIRE = "fire false true";
    private InputFire inputFire;

    @Before
    public void setUp() throws Exception {
        logback.debug(">>>>>>>>> IN");
        inputFire = new InputFire();
    }

    @Test
    public void testSetFire() throws Exception {
        inputFire.setFire(ProtobufTest.getTestInput().getFire());
        assertTrue(inputFire.hasFire());
    }

    @Test
    public void testSendUnitMessageTo() throws MessageNotSentException {
        inputFire.setFire(ProtobufTest.getTestInput().getFire());

        final LegoNxtTank legoNxtTank = createNiceMock(LegoNxtTank.class);
        final Capture<UnitMessage> messageCapture = new Capture<>();
        legoNxtTank.sendUnitMessage(capture(messageCapture));
        expectLastCall().once();
        replay(legoNxtTank);

        inputFire.sendUnitMessageTo(legoNxtTank);
        verify(legoNxtTank);
        assertEquals(UnitMessageType.Command, messageCapture.getValue().getMessageType());
        assertEquals(INPUT_FIRE, messageCapture.getValue().getPayload());
    }

    @After
    public void tearDown() throws Exception {
        logback.debug("<<<< OUT");
    }
}

