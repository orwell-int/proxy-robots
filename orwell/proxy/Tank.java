package orwell.proxy;

import com.google.protobuf.InvalidProtocolBufferException;

import orwell.common.UnitMessage;
import orwell.common.UnitMessageType;
import orwell.messages.Controller.Hello;
import orwell.messages.Controller.Input;
import orwell.messages.Robot;
import orwell.messages.Robot.Register;
import orwell.messages.Robot.RobotState;
import orwell.messages.ServerGame.EnumTeam;
import orwell.messages.ServerGame.Registered;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class Tank implements IRobot {

	private static final double STARTING_LIFE_POINTS = 100;
	private String routingID;
	private String bluetoothName;
	private String bluetoothID;
	private Robot.RobotState.Builder tankStateBuilder = Robot.RobotState
			.newBuilder();
	private Robot.RobotState.Move.Builder moveBuilder = Robot.RobotState.Move
			.newBuilder();
	private Robot.Register.Builder registerBuilder = Robot.Register
			.newBuilder();
	private Input currentControllerInput;
	private Hello currentControllerHello;
	private Registered serverGameRegistered;
	private boolean isControllerReady;
	private String controllerName;
	private NXTInfo nxtInfo;
	private MessageFramework mfTank = new MessageFramework();
	private Camera camera;


	public EnumRegistrationState registrationState = EnumRegistrationState.NOT_REGISTERED;
	private EnumTeam team;


	public Tank(String bluetoothName, String bluetoothID, Camera camera) {
		setBluetoothName(bluetoothName);
		setBluetoothID(bluetoothID);
		this.camera = camera;
		setActive(false);
		setLifePoints(STARTING_LIFE_POINTS);
		setMoveLeft(0);
		setMoveRight(0);
		nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, bluetoothName,
				bluetoothID);
	}

	private void setBluetoothName(String bluetoothName) {
		this.bluetoothName = bluetoothName;
	}

	@Override
	public void setRoutingID(String routingID) {
		this.routingID = routingID;
	}

	private void setBluetoothID(String bluetoothID) {
		this.bluetoothID = bluetoothID;
	}

	@Override
	public void setActive(boolean isActive) {
		tankStateBuilder.setActive(isActive);
	}

	@Override
	public void setLifePoints(double lifePoints) {
		tankStateBuilder.setLife(lifePoints);
	}

	public void setMoveLeft(double moveLeft) {
		moveBuilder.setLeft(moveLeft);
	}

	public void setMoveRight(double moveRight) {
		moveBuilder.setLeft(moveRight);
	}

	public RobotState.Move getRobotStateMove() {
		return moveBuilder.build();
	}

	private RobotState getRobotState() {
		tankStateBuilder.setMove(getRobotStateMove());
		return tankStateBuilder.build();
	}

	private Register getRegister() {
		registerBuilder.setTemporaryRobotId(routingID);
		registerBuilder.setVideoUrl(camera.getURL());
		return registerBuilder.build();
	}

	@Override
	public Input getControllerInput() {
		return currentControllerInput;
	}

	public String getBluetoothName() {
		return bluetoothName;
	}

	public String getBluetoothID() {
		return bluetoothID;
	}

	@Override
	public String getRoutingID() {
		return routingID;
	}

	public boolean getIsControllerReady() {
		return isControllerReady;
	}

	public String getControllerName() {
		return controllerName;
	}
	
	@Override
	public EnumTeam getTeam() {
		return team;
	}

	@Override
	public byte[] getZMQRobotState() {
		String zMQmessageHeader = getRoutingID() + " " + "RobotState" + " ";
		return orwell.proxy.Utils.Concatenate(zMQmessageHeader.getBytes(),
				getRobotState().toByteArray());
	}

	@Override
	public byte[] getZMQRegister() {
		String zMQmessageHeader = getRoutingID() + " " + "Register" + " ";
		return orwell.proxy.Utils.Concatenate(zMQmessageHeader.getBytes(),
				getRegister().toByteArray());
	}

	@Override
	public void setRegistered(byte[] registeredMessage) {
		try {
			this.serverGameRegistered = Registered
					.parseFrom(registeredMessage);
			routingID = serverGameRegistered.getRobotId();
			if (routingID.isEmpty())
				registrationState = EnumRegistrationState.REGISTRATION_FAILED;
			else
			{
				registrationState = EnumRegistrationState.REGISTERED;
				team = serverGameRegistered.getTeam();
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			System.out.println("setRegistered protobuff exception");
			e.printStackTrace();
		}
	}

	@Override
	public void setControllerInput(byte[] inputMessage) {
		try {
			this.currentControllerInput = Input
					.parseFrom(inputMessage);
			if (currentControllerInput.hasMove()) {
				String payloadMove = "input move ";
				payloadMove += currentControllerInput.getMove().getLeft() + " "
						+ currentControllerInput.getMove().getRight();
				UnitMessage msg = new UnitMessage(UnitMessageType.Command,
						payloadMove);
				mfTank.SendMessage(msg);
			}
			if (currentControllerInput.hasFire()
					&& (currentControllerInput.getFire().getWeapon1() || currentControllerInput
							.getFire().getWeapon2())) {
				String payloadFire = "input fire ";
				payloadFire += currentControllerInput.getFire().getWeapon1()
						+ " " + currentControllerInput.getFire().getWeapon2();
				UnitMessage msg = new UnitMessage(UnitMessageType.Command,
						payloadFire);
				mfTank.SendMessage(msg);
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			System.out.println("setControllerInput protobuff exception");
			e.printStackTrace();
		}
	}

	// TODO The following method is probably now obsolete
	public void setControllerHello(byte[] helloMessage) {
		try {
			this.currentControllerHello = Hello
					.parseFrom(helloMessage);
			controllerName = currentControllerHello.getName();
			isControllerReady = currentControllerHello.getReady();
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			System.out.println("setControllerHello protobuff exception");
			e.printStackTrace();
		}
	}

	public NXTInfo getNXTInfo() {
		return nxtInfo;
	}

	@Override
	public boolean connectToRobot() {
		return mfTank.ConnectToNXT(nxtInfo);
	}

	public void register() {

	}

	@Override
	public String toString() {
		String string = "Tank {[BTName] " + getBluetoothName() + " [BTID] "
				+ getBluetoothID() + " [RoutingID] " + getRoutingID() + "}"
				+ "\n\t" + controllerHelloToString() + "\n\t"
				+ controllerInputToString() + "\n\t" + robotStatetoString();
		return string;
	}

	public String robotStatetoString() {
		String string = "RobotState of " + getRoutingID()
				+ "\n\t|___isActive   = " + getRobotState().getActive()
				+ "\n\t|___lifePoints = " + getRobotState().getLife()
				+ "\n\t|___MoveState  = [LEFT] "
				+ getRobotStateMove().getLeft() + " [RIGHT] "
				+ getRobotStateMove().getRight();
		return string;
	}

	public String controllerInputToString() {
		String string;
		if (null != currentControllerInput) {
			string = "Controller INPUT of Robot [" + getRoutingID() + "]:"
					+ "\n\t|___Move order: [LEFT] "
					+ currentControllerInput.getMove().getLeft()
					+ " \t\t[RIGHT] "
					+ currentControllerInput.getMove().getRight()
					+ "\n\t|___Fire order: [WEAPON1] "
					+ currentControllerInput.getFire().getWeapon1()
					+ " \t[WEAPON2] "
					+ currentControllerInput.getFire().getWeapon2();
		} else {
			string = "Controller INPUT of Robot [" + getRoutingID()
					+ "] NOT initialized!";
		}
		return string;
	}

	public String controllerHelloToString() {
		String string;
		if (null != currentControllerHello) {
			string = "Controller HELLO of Robot [" + getRoutingID() + "]:"
					+ "\n\t|___Name: " + currentControllerHello.getName()
					+ "\n\t|___isReady: " + currentControllerHello.getReady();
		} else {
			string = "Controller HELLO of Robot [" + getRoutingID()
					+ "] NOT initialized!";
		}
		return string;
	}

	public String serverGameRegisteredToString() {
		String string;
		if (null != serverGameRegistered) {
			string = "ServerGame REGISTERED of Robot [" + getRoutingID() + "]:"
					+ "\n\t|___final RoutingID: " + serverGameRegistered.getRobotId()
					+ "\n\t|___team: " + serverGameRegistered.getTeam();
		} else {
			string = "ServerGame REGISTERED of Robot [" + getRoutingID()
					+ "] NOT initialized!";
		}
		return string;
	}
}
