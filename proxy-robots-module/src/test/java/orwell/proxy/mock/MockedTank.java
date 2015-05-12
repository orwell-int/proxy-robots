package orwell.proxy.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;
import orwell.messages.Robot;
import orwell.messages.ServerGame;
import orwell.proxy.robot.IRobot;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public class MockedTank implements IRobot {
    final static Logger logback = LoggerFactory.getLogger(MockedTank.class);

    private int getConnectionStateCounter = 0;
    private String routingId = "tempRoutingId";
    private EnumRegistrationState registrationState = EnumRegistrationState.NOT_REGISTERED;
    private EnumConnectionState enumConnectionState = EnumConnectionState.NOT_CONNECTED;
    private byte[] serverRobotStateBytes;
    private byte[] inputBytes;

    @Override
    public String getTeamName() {
        return null;
    }

    @Override
    public EnumRegistrationState getRegistrationState() {

        return registrationState;
    }

    @Override
    public EnumConnectionState getConnectionState() {
        getConnectionStateCounter++;
        return enumConnectionState;
    }

    @Override
    public String getRoutingId() {
        return routingId;
    }

    @Override
    public void setRoutingId(String routingId) {
        this.routingId = routingId;
    }

    @Override
    public String getImage() {
        return null;
    }

    @Override
    public void setImage(String image) {

    }

    @Override
    public byte[] getServerRobotStateBytes_And_ClearDelta() {
        if (null == this.serverRobotStateBytes) {
            Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
            Robot.Rfid.Builder rfidBuilder = Robot.Rfid.newBuilder();
            rfidBuilder.setRfid("1234");
            rfidBuilder.setStatus(Robot.Status.ON);
            rfidBuilder.setTimestamp(1234567890);
            serverRobotStateBuilder.addRfid(rfidBuilder.build());

            this.serverRobotStateBytes = serverRobotStateBuilder.build().toByteArray();
        }
        return this.serverRobotStateBytes;
    }

    @Override
    public void setRegistered(byte[] registeredMessage) {
        registrationState = EnumRegistrationState.REGISTERED;
        this.routingId = "BananaOne";
    }

    @Override
    public byte[] getRegisterBytes() {
        ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId("BananaOne");
        registeredBuilder.setTeam("BLUE");

        return registeredBuilder.build().toByteArray();
    }

    public byte[] getControllerInputBytes() {
        return this.inputBytes;
    }

    @Override
    public Controller.Input getControllerInput() {
        return null;
    }

    @Override
    public void setControllerInput(byte[] inputMessage) {
        this.inputBytes = inputMessage;
    }

    @Override
    public EnumConnectionState connectToDevice() {
        logback.info("Connecting to physical device");
        enumConnectionState = EnumConnectionState.CONNECTED;
        return enumConnectionState;
    }

    @Override
    public void closeConnection() {
        logback.info("Closing Connection");
        enumConnectionState = EnumConnectionState.NOT_CONNECTED;
    }

    @Override
    public String robotStateToString() {
        return null;
    }

    @Override
    public String controllerInputToString() {
        return null;
    }

    @Override
    public String serverGameRegisteredToString() {
        return null;
    }

    @Override
    public void buildRegister() {

    }
}
