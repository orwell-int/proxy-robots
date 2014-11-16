package orwell.proxy;

import java.util.UUID;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import lejos.mf.pc.MessageFramework;
import orwell.messages.Controller.Input;
import orwell.messages.Robot.Register;
import orwell.messages.Robot.RobotState;
import orwell.messages.ServerGame.EnumTeam;
import orwell.messages.ServerGame.Registered;

import com.google.protobuf.InvalidProtocolBufferException;

public class Tank implements IRobot {
	final static Logger logback = LoggerFactory.getLogger(Tank.class); 

	private static final double STARTING_LIFE_POINTS = 100;
	private String routingID = UUID.randomUUID().toString();
	private String bluetoothName;
	private String bluetoothID;
	private RobotState.Builder tankStateBuilder = RobotState
			.newBuilder();
	private RobotState.Move.Builder moveBuilder = RobotState.Move
			.newBuilder();
	private Register.Builder registerBuilder = Register
			.newBuilder();
	private Input currentControllerInput;
	private Registered serverGameRegistered;
	private NXTInfo nxtInfo;
	private MessageFramework mfTank;
	private Camera camera;
	private Register register;
	private String image;

	private EnumRegistrationState registrationState = EnumRegistrationState.NOT_REGISTERED;
	private EnumConnectionState connectionState = EnumConnectionState.NOT_CONNECTED;
	private EnumTeam team;

	public Tank(String bluetoothName, String bluetoothID, Camera camera,
			MessageFramework mf, String image) {
		setBluetoothName(bluetoothName);
		setBluetoothID(bluetoothID);
		this.camera = camera;
		setActive(false);
		setLifePoints(STARTING_LIFE_POINTS);
		setMoveLeft(0);
		setMoveRight(0);
		nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, bluetoothName,
				bluetoothID);
		mfTank = mf;
		this.image = image;
	}

	public Tank(String bluetoothName, String bluetoothID, Camera camera, String image) {
		this(bluetoothName, bluetoothID, camera, new MessageFramework(), image);
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

	private void setMoveLeft(double moveLeft) {
		moveBuilder.setLeft(moveLeft);
	}

	private void setMoveRight(double moveRight) {
		moveBuilder.setLeft(moveRight);
	}

	private RobotState.Move getRobotStateMove() {
		return moveBuilder.build();
	}

	private RobotState getRobotState() {
		tankStateBuilder.setMove(getRobotStateMove());
		return tankStateBuilder.build();
	}

	@Override
	public void buildRegister() {
		registerBuilder.setTemporaryRobotId(routingID);
		registerBuilder.setVideoUrl(camera.getURL());
		registerBuilder.setImage(image);
		if("" == image)
		{
			logback.info("Image of tank " + routingID + " is empty. " 
					+ "This will probably be an issue for the serverGame");
		}
		register = registerBuilder.build();
	}

	private Register getRegister() {
		if(null == register)
		{
			buildRegister();
		}
		return register;
	}

	@Override
	public Input getControllerInput() {
		return currentControllerInput;
	}

	private String getBluetoothName() {
		return bluetoothName;
	}

	private String getBluetoothID() {
		return bluetoothID;
	}

	@Override
	public String getRoutingID() {
		return routingID;
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
		byte[] zmqRegister = orwell.proxy.Utils.Concatenate(zMQmessageHeader.getBytes(),
				getRegister().toByteArray());
		logback.info("zMQmessageHeader: " + zMQmessageHeader);
		return zmqRegister;
	}

	@Override
	public void setRegistered(byte[] registeredMessage) {
		try {
			this.serverGameRegistered = Registered.parseFrom(registeredMessage);
			routingID = serverGameRegistered.getRobotId();
			if (routingID.isEmpty())
				registrationState = EnumRegistrationState.REGISTRATION_FAILED;
			else {
				registrationState = EnumRegistrationState.REGISTERED;
				team = serverGameRegistered.getTeam();
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			logback.info("setRegistered protobuff exception");
			e.printStackTrace();
		}
	}

	@Override
	public void setControllerInput(byte[] inputMessage) {
		try {
			this.currentControllerInput = Input.parseFrom(inputMessage);
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
			logback.info("setControllerInput protobuff exception");
			e.printStackTrace();
		}
	}

	@Override
	public EnumConnectionState connectToRobot() {
		Boolean isConnected = mfTank.ConnectToNXT(nxtInfo);
		if (isConnected) {
			this.connectionState = EnumConnectionState.CONNECTED;
		} else {
			this.connectionState = EnumConnectionState.CONNECTION_FAILED;
		}
		return this.connectionState;
	}

	@Override
	public String toString() {
		String string = "Tank {[BTName] " + getBluetoothName() + " [BTID] "
				+ getBluetoothID() + " [RoutingID] " + getRoutingID() + "}"
				+ "\n\t" + controllerInputToString() + "\n\t"
				+ robotStatetoString();
		return string;
	}

	@Override
	public String robotStatetoString() {
		String string = "RobotState of " + getRoutingID()
				+ "\n\t|___isActive   = " + getRobotState().getActive()
				+ "\n\t|___lifePoints = " + getRobotState().getLife()
				+ "\n\t|___MoveState  = [LEFT] "
				+ getRobotStateMove().getLeft() + " [RIGHT] "
				+ getRobotStateMove().getRight();
		return string;
	}

	@Override
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

	@Override
	public String serverGameRegisteredToString() {
		String string;
		if (null != serverGameRegistered) {
			string = "ServerGame REGISTERED of Robot [" + getRoutingID() + "]:"
					+ "\n\t|___final RoutingID: "
					+ serverGameRegistered.getRobotId() + "\n\t|___team: "
					+ serverGameRegistered.getTeam();
		} else {
			string = "ServerGame REGISTERED of Robot [" + getRoutingID()
					+ "] NOT initialized!";
		}
		return string;
	}

	@Override
	public EnumRegistrationState getRegistrationState() {
		return registrationState;
	}

	@Override
	public EnumConnectionState getConnectionState() {
		return connectionState;
	}

	@Override
	public EnumTeam getTeam() {
		return team;
	}

	@Override
	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String getImage() {
		return image;
	}
}
