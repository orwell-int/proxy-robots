package orwell.robots;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;
import orwell.common.MessageListenerInterface;
import orwell.common.UnitMessage;

/**
 * Thread to wait for a Bluetooth connection and execute remote commands
 */
class RemoteControl extends Thread implements MessageListenerInterface {
	private int speed;
	private final static int DIFF_SPEED = 30;
	protected volatile boolean remoteCtrlAlive;

	DifferentialPilot pilot;

	public void run() {
		remoteCtrlAlive = true;
		pilot = new DifferentialPilot(3.0f, 12.5f, Motor.B, Motor.C);

		LCD.drawString(" Waiting for PC ", 4, 10, true);
		MessageFrameworkNXT mfw = MessageFrameworkNXT.getInstance();
		mfw.addMessageListener(this);
		mfw.StartListen();
		LCD.drawString("Connected!", 0, 0, true);

		speed = 500;

		while (!Button.ESCAPE.isPressed() && remoteCtrlAlive) {
		}
	}

	public void stop_robot() {
		remoteCtrlAlive = false;
	}

	public static void main(String[] args) {
		new RemoteControl().run();
	}

	// public boolean isAlive(){
	// return remoteCtrlAlive;
	// }

	@Override
	public void recievedNewMessage(UnitMessage msg) {
		LCD.drawString("Command: " + msg.getPayload(), 0, 5);
		if (msg.getPayload().equals("stop")) {
			pilot.stop();
			LCD.clearDisplay();
			LCD.drawString("STOP", 0, 6);
		} else if (msg.getPayload().equals("forward")) {
			pilot.backward(); // This is due to the actual geometry of the tank
			LCD.clearDisplay();
			LCD.drawString("FORWARD", 0, 6);
		} else if (msg.getPayload().equals("backward")) {
			pilot.forward(); // This is due to the actual geometry of the tank
			LCD.clearDisplay();
			LCD.drawString("BACKWARD", 0, 6);
		} else if (msg.getPayload().equals("left")) {
			pilot.steer(200, -45, true);
			LCD.clearDisplay();
			LCD.drawString("LEFT", 0, 6);
		} else if (msg.getPayload().equals("right")) {
			pilot.steer(200, 45, true);
			LCD.clearDisplay();
			LCD.drawString("RIGHT", 0, 6);
		} else if (msg.getPayload().equals("fwdL")) {
			pilot.arc(-20, 360, true);
			LCD.clearDisplay();
			LCD.drawString("FwdL", 0, 6);
		} else if (msg.getPayload().equals("fwdR")) {
			pilot.arc(20, 360, true);
			LCD.clearDisplay();
			LCD.drawString("FwdR", 0, 6);
		} else if (msg.getPayload().equals("rwdL")) {
			pilot.arc(-20, -360, true);
			LCD.clearDisplay();
			LCD.drawString("RwdL", 0, 6);
		} else if (msg.getPayload().equals("rwdR")) {
			pilot.arc(20, -360, true);
			LCD.clearDisplay();
			LCD.drawString("RwdR", 0, 6);
		} else if (msg.getPayload().equals("increaseSpeed")) {
			speed = speed + DIFF_SPEED;
			if (speed <= 0)
				speed = 0;
			pilot.setTravelSpeed(speed);
			LCD.clearDisplay();
			LCD.drawString("INCREASE SPEED", 0, 6);
		} else if (msg.getPayload().equals("decreaseSpeed")) {
			speed = speed - DIFF_SPEED;
			if (speed >= 800)
				speed = 800;
			pilot.setTravelSpeed(speed);
			LCD.clearDisplay();
			LCD.drawString("DECREASE SPEED", 0, 6);
		} else if (msg.getPayload().equals("stopPrg")) {
			pilot.stop();
			LCD.clearDisplay();
			LCD.drawString("PROGRAM STOPPED", 0, 6);
			stop_robot();
		} else if (msg.getPayload().equals("planted")) {
			LCD.clearDisplay();
			LCD.drawString("Bomb planted", 0, 6);
		} else {
			LCD.drawString("No match", 0, 6);
		}

	}
}
