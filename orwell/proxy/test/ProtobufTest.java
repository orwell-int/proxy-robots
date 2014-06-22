package orwell.proxy.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.protobuf.InvalidProtocolBufferException;

import orwell.messages.Controller;
import orwell.messages.Controller.Input;
import orwell.proxy.Tank;

/**
 * Tests for {@link Tank}.
 * 
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ProtobufTest {

	private Controller.Input buildTestInput() {
		Controller.Input.Builder testInput = Input.newBuilder();
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
	public void checkControllerInput() {
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
			// TODO Auto-generated catch block
			System.out.println("setControllerInput protobuff exception");
			fail(e.toString());
		}
	}

}
