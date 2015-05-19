package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public abstract class IRobot2 implements IRobotElement, IRobotInput {

    public String routingId;
    public EnumRegistrationState registrationState;
    public String cameraUrl;
    public String image;
    public String teamName;
    protected EnumConnectionState connectionState;

    abstract void sendUnitMessage(UnitMessage unitMessage);

    abstract EnumConnectionState connect();

    abstract void closeConnection();

    EnumConnectionState getConnectionState() {
        return connectionState;
    }
}
