package orwell.proxy;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;

import static org.junit.Assert.*;

/**
 * Tests for {@link Controller.Input}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ProtobufTest {
    private final static double LEFT_MOVE = 50;
    private final static double RIGHT_MOVE = 0.75;
    private final static Logger logback = LoggerFactory.getLogger(ProtobufTest.class);

    public static Controller.Input buildTestInput() {
        final Controller.Input.Builder testInput = Controller.Input.newBuilder();
        final Controller.Input.Move.Builder testMove = Controller.Input.Move
                .newBuilder();
        testMove.setLeft(LEFT_MOVE);
        testMove.setRight(RIGHT_MOVE);

        testInput.setMove(testMove.build());

        final Controller.Input.Fire.Builder testFire = Controller.Input.Fire
                .newBuilder();
        testFire.setWeapon1(false);
        testFire.setWeapon2(true);
        testInput.setFire(testFire);

        return testInput.build();
    }

    @Test
    public void testControllerInput() {
        logback.debug("IN");

        final Controller.Input input;

        try {
            input = Controller.Input.parseFrom(buildTestInput().toByteArray());

            assertTrue("Input contains Move data", input.hasMove());
            assertEquals(LEFT_MOVE, input.getMove().getLeft(), 0);
            assertEquals(RIGHT_MOVE, input.getMove().getRight(), 0);

            assertTrue("Input contains Fire data", input.hasFire());
            assertFalse(input.getFire().getWeapon1());
            assertTrue(input.getFire().getWeapon2());
        } catch (final InvalidProtocolBufferException e) {
            logback.error("setControllerInput protobuf exception");
            logback.error(e.getMessage());
            fail(e.toString());
        }
        logback.debug("OUT");
    }

}
