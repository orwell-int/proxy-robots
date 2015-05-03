package orwell.proxy;

import orwell.messages.Controller.Input;

public interface IRobot {

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

    String getTeamName();

    EnumRegistrationState getRegistrationState();

    EnumConnectionState getConnectionState();

    void setRoutingID(String routingID);

    String getRoutingID();

    /*
     * Image is a picture of the robot itself to be sent to the
     * server before the start of the game
     */
    void setImage(String image);

    String getImage();

    /*
     * This will clear the current ServerRobotState
     */
    byte[] getAndClearZmqServerRobotStateBytes();


    void setRegistered(byte[] registeredMessage);

    byte[] getZmqRegister();

    byte[] getRegisterBytes();

    void setControllerInput(byte[] inputMessage);

    Input getControllerInput();

    EnumConnectionState connectToDevice();

    void closeConnection();

    String robotStateToString();

    String controllerInputToString();

    String serverGameRegisteredToString();

    void buildRegister();
}
