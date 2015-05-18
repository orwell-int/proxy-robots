package orwell.proxy.robot;

import com.google.protobuf.InvalidProtocolBufferException;
import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import lejos.mf.pc.MessageFramework;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller.Input;
import orwell.messages.Robot.Register;
import orwell.messages.Robot.ServerRobotState;
import orwell.messages.ServerGame.Registered;

import java.util.UUID;

public class Tank implements IRobot, MessageListenerInterface {
    private final static Logger logback = LoggerFactory.getLogger(Tank.class);


    @Override
    public void receivedNewMessage(final UnitMessage msg) {

    }























    private final Register.Builder registerBuilder = Register
            .newBuilder();
    private  NXTInfo nxtInfo;
    private  MessageFramework mfTank;
    private  ICamera camera;
    private final TankDeltaState tankDeltaState = new TankDeltaState();
    private String routingID = UUID.randomUUID().toString();
    private String bluetoothName;
    private String bluetoothID;
    private Input currentControllerInput;
    private Registered serverGameRegistered;
    private Register register;
    private String image;
    private EnumRegistrationState registrationState = EnumRegistrationState.NOT_REGISTERED;
    private EnumConnectionState connectionState = EnumConnectionState.NOT_CONNECTED;
    private String teamName;

    private Tank(final String bluetoothName, final String bluetoothID, final ICamera camera,
                 final MessageFramework mf, final String image) {
        setBluetoothName(bluetoothName);
        setBluetoothID(bluetoothID);
        this.camera = camera;
        nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, bluetoothName,
                bluetoothID);
        mfTank = mf;
        mf.addMessageListener(this);
        this.image = image;
    }

    public Tank(final String bluetoothName, final String bluetoothID, final ICamera camera, final String image) {
        this(bluetoothName, bluetoothID, camera, new MessageFramework(), image);
    }

    @Override
    public void buildRegister() {
        registerBuilder.setTemporaryRobotId(routingID);
        registerBuilder.setVideoUrl(camera.getUrl());
        registerBuilder.setImage(image);
        if (0 == image.compareTo("")) {
            logback.warn("Image of tank " + routingID + " is empty. "
                    + "This will probably be an issue for the serverGame");
        }
        register = registerBuilder.build();
    }

    private Register getRegister() {
        if (null == register) {
            buildRegister();
        }
        return register;
    }

    @Override
    public byte[] getRegisterBytes() {
        final Register register = getRegister();
        if (null != register)
            return register.toByteArray();
        else
            return null;
    }

    @Override
    public Input getControllerInput() {
        return currentControllerInput;
    }

    @Override
    public void setControllerInput(final byte[] inputMessage) {
        try {
            this.currentControllerInput = Input.parseFrom(inputMessage);
            if (currentControllerInput.hasMove()) {
                String payloadMove = "input move ";
                payloadMove += currentControllerInput.getMove().getLeft() + " "
                        + currentControllerInput.getMove().getRight();
                final UnitMessage msg = new UnitMessage(UnitMessageType.Command,
                        payloadMove);
                mfTank.SendMessage(msg);
            }
            if (currentControllerInput.hasFire()
                    && (currentControllerInput.getFire().getWeapon1() ||
                    currentControllerInput.getFire().getWeapon2())
                    ) {
                String payloadFire = "input fire ";
                payloadFire += currentControllerInput.getFire().getWeapon1()
                        + " " + currentControllerInput.getFire().getWeapon2();
                final UnitMessage msg = new UnitMessage(UnitMessageType.Command,
                        payloadFire);
                mfTank.SendMessage(msg);
            }
        } catch (final InvalidProtocolBufferException e) {
            logback.info("setControllerInput protobuf exception");
            logback.error(e.getMessage());
        }
    }

    private String getBluetoothName() {
        return bluetoothName;
    }

    private void setBluetoothName(final String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    private String getBluetoothID() {
        return bluetoothID;
    }

    private void setBluetoothID(final String bluetoothID) {
        this.bluetoothID = bluetoothID;
    }

    @Override
    public String getRoutingId() {
        return routingID;
    }

    @Override
    public void setRoutingId(final String routingId) {
        this.routingID = routingId;
    }

    @Override
    public byte[] getServerRobotStateBytes_And_ClearDelta() {
        final ServerRobotState srs = getTankDeltaState().getServerRobotState_And_ClearDelta();
        if (null != srs)
            return srs.toByteArray();
        else
            return null;
    }

    @Override
    public void setRegistered(final byte[] registeredMessage) {
        try {
            this.serverGameRegistered = Registered.parseFrom(registeredMessage);
            routingID = serverGameRegistered.getRobotId();
            if (routingID.isEmpty()) {
                registrationState = EnumRegistrationState.REGISTRATION_FAILED;
                logback.warn("Registration of robot: " + serverGameRegisteredToString() + " FAILED");
            } else {
                registrationState = EnumRegistrationState.REGISTERED;
                teamName = serverGameRegistered.getTeam();
                logback.info("Registered robot: " + serverGameRegisteredToString());
            }
        } catch (final InvalidProtocolBufferException e) {
            logback.error("setRegistered protobuf exception: " + e.getMessage());
        }
    }

    @Override
    public EnumConnectionState connectToDevice() {
        logback.info("Connecting to robot: \n" + toString());

        final Boolean isConnected = mfTank.ConnectToNXT(nxtInfo);
        if (isConnected) {
            this.connectionState = EnumConnectionState.CONNECTED;
            logback.info("Robot [" + getRoutingId()
                    + "] is isConnected to the proxy!");
        } else {
            this.connectionState = EnumConnectionState.CONNECTION_FAILED;
            logback.warn("Robot [" + getRoutingId()
                    + "] failed to connect to the proxy!");
        }
        return this.connectionState;
    }

    @Override
    public String toString() {
        return "Tank {[BTName] " + getBluetoothName() + " [BT-ID] "
                + getBluetoothID() + " [RoutingID] " + getRoutingId() + "}"
                + "\n\t" + controllerInputToString() + "\n\t"
                + robotStateToString();
    }

    @Override
    public String robotStateToString() {
        return "RobotState of " + getRoutingId()
                + "\n\t|___RFID   = " + getTankDeltaState().getServerRobotState().getRfidList()
                + "\n\t|___Colour = " + getTankDeltaState().getServerRobotState().getColourList();
    }

    @Override
    public String controllerInputToString() {
        final String string;
        if (null != currentControllerInput) {
            string = "Controller INPUT of Robot [" + getRoutingId() + "]:"
                    + "\n\t|___Move order: [LEFT] "
                    + currentControllerInput.getMove().getLeft()
                    + " \t\t[RIGHT] "
                    + currentControllerInput.getMove().getRight()
                    + "\n\t|___Fire order: [WEAPON1] "
                    + currentControllerInput.getFire().getWeapon1()
                    + " \t[WEAPON2] "
                    + currentControllerInput.getFire().getWeapon2();
        } else {
            string = "Controller INPUT of Robot [" + getRoutingId()
                    + "] NOT initialized!";
        }
        return string;
    }

    @Override
    public String serverGameRegisteredToString() {
        final String string;
        if (null != serverGameRegistered) {
            string = "ServerGame REGISTERED of Robot [" + getRoutingId() + "]:"
                    + "\n\t|___final RoutingID: "
                    + serverGameRegistered.getRobotId() + "\n\t|___team: "
                    + serverGameRegistered.getTeam();
        } else {
            string = "ServerGame REGISTERED of Robot [" + getRoutingId()
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
    public String getTeamName() {
        return teamName;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public void setImage(final String image) {
        this.image = image;
    }

    public void closeConnection() {
        if (EnumConnectionState.CONNECTED == connectionState) {
            mfTank.close();
        }
    }

//    public void receivedNewMessage(final UnitMessage msg) {
//        switch (msg.getMsgType()) {
//            case Stop:
//                onMsgStop();
//                break;
//            case Rfid:
//                onMsgRfid(msg.getPayload());
//                break;
//            case Command:
//                onMsgCommand(msg.getPayload());
//                break;
//            case Colour:
//                onMsgColour(msg.getPayload());
//                break;
//            default:
//                onMsgNotDefined(msg.getPayload());
//                break;
//        }
//    }

    private void onMsgStop() {
        logback.info("Tank " + this.getBluetoothName() + " is stopping");
        connectionState = EnumConnectionState.NOT_CONNECTED;
        closeConnection();
    }

    private void onMsgRfid(final String rfidValue) {
        logback.debug("RFID info received: " + rfidValue);
        getTankDeltaState().setNewState(EnumSensor.RFID, rfidValue);
    }

    private void onMsgColour(final String colourValue) {
        logback.debug("Colour info received: " + colourValue);
        getTankDeltaState().setNewState(EnumSensor.COLOUR, colourValue);
    }

    private void onMsgCommand(final String msg) {
        logback.debug("Tank is sending a command: " + msg);
        logback.debug("This command will not be processed");
    }

    private void onMsgNotDefined(final String msg) {
        logback.error("Unable to decode message received: " + msg);
    }

    private TankDeltaState getTankDeltaState() {
        return this.tankDeltaState;
    }


}
