package orwell.proxy;

import orwell.messages.Controller.Input;

public interface IRobot {

    String getTeamName();

    EnumRegistrationState getRegistrationState();

    EnumConnectionState getConnectionState();

    String getRoutingID();

    void setRoutingID(String routingID);

    String getImage();

    /*
     * Image is a picture of the robot itself to be sent to the
     * server before the start of the game
     */
    void setImage(String image);

    /*
     * This will clear the current ServerRobotState
     */
    byte[] getAndClearZmqServerRobotStateBytes();

    void setRegistered(byte[] registeredMessage);

    byte[] getZmqRegister();

    byte[] getRegisterBytes();

    Input getControllerInput();

    void setControllerInput(byte[] inputMessage);

    EnumConnectionState connectToDevice();

    void closeConnection();

    String robotStateToString();

    String controllerInputToString();

    String serverGameRegisteredToString();

    void buildRegister();

    enum EnumRegistrationState {
        NOT_REGISTERED,
        REGISTERED,
        REGISTRATION_FAILED
    }

    enum EnumConnectionState {
        NOT_CONNECTED,
        CONNECTED,
        CONNECTION_FAILED
    }
}
