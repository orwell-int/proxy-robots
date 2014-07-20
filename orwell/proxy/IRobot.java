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
	
	public void setActive(boolean isActive);
	public void setLifePoints(double lifePoints);
	
	public byte[] getZMQRobotState();
	
	public void setRegistered(byte[] registeredMessage);
	public byte[] getZMQRegister();
	
	public void setControllerInput(byte[] inputMessage);
	public Input getControllerInput();
	
	public EnumConnectionState connectToRobot();
	
	public String robotStatetoString();
	public String controllerInputToString();
	public String serverGameRegisteredToString();
	
}
