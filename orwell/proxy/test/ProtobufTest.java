package orwell.proxy.test;

import orwell.messages.Controller;
import orwell.proxy.FakeServer;
import orwell.proxy.Tank;

public class ProtobufTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    Tank tank = new Tank("Daneel", "001653119482");
	    tank.setNetworkID("BananaOne");
	    System.out.println("Protobuf: Building robot for test: \n" + tank.toString());

	    Controller.Input tankInput = FakeServer.buildTestInput();
	    tank.setControllerInput(tankInput.toByteArray());
	    System.out.println(tank.controllerInputToString());
	}

}
