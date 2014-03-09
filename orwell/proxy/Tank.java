package orwell.proxy;


import com.google.protobuf.InvalidProtocolBufferException;

import orwell.messages.Controller;
import orwell.messages.Controller.Hello;
import orwell.messages.Controller.Input;
import orwell.messages.Robot;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class Tank {

	private static final double STARTING_LIFE_POINTS = 100;
	private String routingID;
	private String bluetoothName;
	private String bluetoothID;
	private Robot.RobotState.Builder tankStateBuilder = Robot.RobotState.newBuilder();
	private Robot.RobotState.Move.Builder moveBuilder = Robot.RobotState.Move.newBuilder();
	private Input currentControllerInput;
	private Hello currentControllerHello;
	private boolean isControllerReady;
	private String controllerName;
	private NXTInfo nxtInfo;
	private MessageFramework mfTank = new MessageFramework();

	public Tank(String bluetoothName, String bluetoothID)
	{
		setBluetoothName(bluetoothName);
		setBluetoothID(bluetoothID);
		setActive(false);
		setLifePoints(STARTING_LIFE_POINTS);
		setMoveLeft(0);
		setMoveRight(0);
		nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, bluetoothName, bluetoothID);
	}

	private void setBluetoothName(String bluetoothName) {
		this.bluetoothName = bluetoothName;
	}

	public void setNetworkID(String networkID) {
		this.routingID = networkID;
	}

	private void setBluetoothID(String bluetoothID) {
		this.bluetoothID = bluetoothID;
	}

	public void setActive(boolean isActive)
	{
		tankStateBuilder.setActive(isActive);
	}

	public void setLifePoints(double lifePoints)
	{
		tankStateBuilder.setLife(lifePoints);
	}

	public void setMoveLeft(double moveLeft)
	{
		moveBuilder.setLeft(moveLeft);
	}

	public void setMoveRight(double moveRight)
	{
		moveBuilder.setLeft(moveRight);
	}

	public Robot.RobotState.Move getRobotStateMove()
	{
		return moveBuilder.build();
	}

	public Robot.RobotState getRobotState()
	{
		tankStateBuilder.setMove(getRobotStateMove());
		return tankStateBuilder.build();
	}

	public Controller.Input getControllerInput()
	{
		return currentControllerInput;
	}

	public String getBluetoothName()
	{
		return bluetoothName;
	}

	public String getBluetoothID()
	{
		return bluetoothID;
	}

	public String getNetworkID()
	{
		return routingID;
	}

	public boolean getIsControllerReady()
	{
		return isControllerReady;
	}

	public String getControllerName()
	{
		return controllerName;
	}

	public byte[] getZMQRobotState()
	{
		String zMQmessageHeader= getNetworkID() + " " + "RobotState" + " ";
		return orwell.proxy.Utils.Concatenate(
				zMQmessageHeader.getBytes(), getRobotState().toByteArray());
	}

	public void setControllerInput(byte [] inputMessage)
	{	
		try {
			this.currentControllerInput = Controller.Input.parseFrom(inputMessage);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			System.out.println("setControllerInput protobuff exception");
			e.printStackTrace();
		}
	}

	public void setControllerHello(byte [] helloMessage) {
		try {
			this.currentControllerHello = Controller.Hello.parseFrom(helloMessage);
			controllerName = currentControllerHello.getName();
			isControllerReady = currentControllerHello.getReady();
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			System.out.println("setControllerHello protobuff exception");
			e.printStackTrace();
		}
	}
	
	public NXTInfo getNXTInfo()
	{
		return nxtInfo;
	}
	
	public void connectToNXT()
	{
		mfTank.ConnectToNXT(nxtInfo);
	}

	public String toString()
	{
		String string = "Tank {[BTName] " + getBluetoothName() + 
				" [BTID] " + getBluetoothID() +
				" [NetworkID] " + getNetworkID() + "}" +
				"\n\t" + controllerHelloToString() +
				"\n\t" + controllerInputToString() +
				"\n\t" + robotStatetoString();
		return string;
	}

	public String robotStatetoString()
	{
		String string = "RobotState of " +  getNetworkID() +
				"\n\t|___isActive   = " + getRobotState().getActive() +
				"\n\t|___lifePoints = " + getRobotState().getLife() +
				"\n\t|___MoveState  = [LEFT] " + getRobotStateMove().getLeft() +
				" [RIGHT] " + getRobotStateMove().getRight();
		return string;
	}

	public String controllerInputToString() 
	{
		String string;
		if (null != currentControllerInput)
		{
			string = "Controller INPUT of Robot [" + getNetworkID() +"]:" +
					"\n\t|___Move order: [LEFT] " + currentControllerInput.getMove().getLeft() + " \t\t[RIGHT] " + currentControllerInput.getMove().getRight() +
					"\n\t|___Fire order: [WEAPON1] " + currentControllerInput.getFire().getWeapon1() + " \t[WEAPON2] " + currentControllerInput.getFire().getWeapon2();
		} else
		{
			string = "Controller INPUT of Robot [" + getNetworkID() +"] NOT initialized!";
		}
		return string;
	}

	public String controllerHelloToString() 
	{
		String string;
		if (null != currentControllerHello)
		{
			string = "Controller HELLO of Robot [" + getNetworkID() +"]:" +
					"\n\t|___Name: " + currentControllerHello.getName() +
					"\n\t|___isReady: " + currentControllerHello.getReady();
		} else
		{
			string = "Controller HELLO of Robot [" + getNetworkID() +"] NOT initialized!";
		}
		return string;
	}

}
