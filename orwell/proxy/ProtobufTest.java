package orwell.proxy;

import orwell.messages.Controller;

public class ProtobufTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    Tank tank = new Tank("Daneel", "001653119482");
	    tank.setNetworkID("BananaOne");
	    System.out.println("ProtobufTest: Building robot for test: \n" + tank.toString());

	    Controller.Input tankInput = FakeServer.buildTestInput();
	    tank.setControllerInput(tankInput.toByteArray());
	    System.out.println(tank.controllerInputToString());
	}

}
