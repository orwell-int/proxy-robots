package orwell.proxy;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;
import orwell.proxy.robot.Tank;

import static org.junit.Assert.*;

/**
 * Tests for {@link Tank}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ProtobufTest {
    final static Logger logback = LoggerFactory.getLogger(ProtobufTest.class);

    public static Controller.Input buildTestInput() {
        Controller.Input.Builder testInput = Controller.Input.newBuilder();
        Controller.Input.Move.Builder testMove = Controller.Input.Move
                .newBuilder();
        testMove.setLeft(50);
        testMove.setRight(0.234);

        testInput.setMove(testMove.build());

        Controller.Input.Fire.Builder testFire = Controller.Input.Fire
                .newBuilder();
        testFire.setWeapon1(false);
        testFire.setWeapon2(true);
        testInput.setFire(testFire);

        return testInput.build();
    }

    @Test
    public void testControllerInput() {
        logback.debug("IN");

        Controller.Input input;

        try {
            input = Controller.Input.parseFrom(buildTestInput().toByteArray());

            assertTrue("Input contains Move data", input.hasMove());
            assertEquals(50, input.getMove().getLeft(), 0);
            assertEquals(0.234, input.getMove().getRight(), 0);

            assertTrue("Input contains Fire data", input.hasFire());
            assertFalse(input.getFire().getWeapon1());
            assertTrue(input.getFire().getWeapon2());
        } catch (InvalidProtocolBufferException e) {
            logback.error("setControllerInput protobuff exception");
            logback.error(e.getMessage());
            fail(e.toString());
        }
        logback.debug("OUT");
    }

}
