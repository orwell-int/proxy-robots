package orwell.proxy;

import orwell.messages.Controller.Input;
import orwell.messages.ServerGame.EnumTeam;

public interface IRobot {
	
	public enum EnumRegistrationState {
		NOT_REGISTERED,
		REGISTERED,
		REGISTRATION_FAILED;
	}
	
	public enum EnumConnectionState {
		NOT_CONNECTED,
		CONNECTED,
		CONNECTION_FAILED;
	}
	
	public EnumTeam getTeam();
	public EnumRegistrationState getRegistrationState();
	public EnumConnectionState getConnectionState();

	public void setRoutingID(String routingID);
	public String getRoutingID();
	
	/*
	 * Image is a picture of the robot itself to be sent to the
	 * server before the start of the game
	 */
	public void setImage(String image);
	public String getImage();
	
	public void setActive(boolean isActive);
	public void setLifePoints(double lifePoints);
	
	public byte[] getZMQRobotState();
	
	public void setRegistered(byte[] registeredMessage);
	public byte[] getZMQRegister();
	
	public void setControllerInput(byte[] inputMessage);
	public Input getControllerInput();
	
	public EnumConnectionState connectToRobot();
	
	public String robotStateToString();
	public String controllerInputToString();
	public String serverGameRegisteredToString();
	public void buildRegister();
}
