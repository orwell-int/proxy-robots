package orwell.proxy.robot;

import lejos.mf.common.IUnitMessage;

import java.util.UUID;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public abstract class IRobot implements IRobotElement, IRobotInput {

    private String routingId = UUID.randomUUID().toString();
    private String cameraUrl;
    private String image;
    private String teamName = "";
    private EnumRegistrationState registrationState = EnumRegistrationState.NOT_REGISTERED;
    private EnumConnectionState connectionState = EnumConnectionState.NOT_CONNECTED;
    private EnumRobotVictoryState victoryState = EnumRobotVictoryState.WAITING_FOR_START;

    public abstract void sendUnitMessage(IUnitMessage unitMessage);

    public abstract EnumConnectionState connect();

    public abstract void closeConnection();


    public String getRoutingId() {
        return routingId;
    }

    public void setRoutingId(final String routingId) {
        this.routingId = routingId;
    }

    public String getCameraUrl() {
        return cameraUrl;
    }

    protected void setCameraUrl(final String cameraUrl) {
        this.cameraUrl = cameraUrl;
    }

    String getImage() {
        return image;
    }

    protected void setImage(final String image) {
        this.image = image;
    }

    public String getTeamName() {
        return teamName;
    }

    protected void setTeamName(final String teamName) {
        this.teamName = teamName;
    }

    public EnumRegistrationState getRegistrationState() {
        return registrationState;
    }

    protected void setRegistrationState(final EnumRegistrationState registrationState) {
        this.registrationState = registrationState;
    }

    public EnumConnectionState getConnectionState() {
        return connectionState;
    }

    protected void setConnectionState(final EnumConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public EnumRobotVictoryState getVictoryState() {
        return victoryState;
    }

    protected void setVictoryState(final EnumRobotVictoryState victoryState) {
        this.victoryState = victoryState;
    }

    public abstract void setRfidValue(String rfidValue);

    public abstract void setColourValue(String colourValue);
}
