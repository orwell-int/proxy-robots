package orwell.robots;

import orwell.common.MessageListenerInterface;
import orwell.common.UnitMessage;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.Sound;

/** 
 * Thread to wait for a Bluetooth connection and execute remote commands 
 */ 
class TankControl extends Thread implements MessageListenerInterface { 
        protected volatile boolean remoteCtrlAlive;
  
        NXTMotor motorLeft = new NXTMotor(MotorPort.B);
        NXTMotor motorRight = new NXTMotor(MotorPort.C);
          
        public void run() 
        {
            remoteCtrlAlive = true;
        
            LCD.drawString(" Waiting for PC ", 4, 10, true);
            MessageFrameworkNXT mfw = MessageFrameworkNXT.getInstance();
            mfw.addMessageListener(this);
            mfw.StartListen();
            LCD.drawString("Connected!",0,0, true);
            
            while(!Button.ESCAPE.isDown() && remoteCtrlAlive) {} 
        }
          
        public void stop() {
                remoteCtrlAlive = false;
        }


        public static void main(String[] args) { 
                new TankControl().run(); 
        }
//      public boolean isAlive(){
//              return remoteCtrlAlive;
//      }
        
        public void recievedNewMessage(UnitMessage msg) {         
                LCD.drawString("Command: "+ msg.getPayload(), 0, 5);
                if(msg.getPayload().equals("stop"))
                {
                	motorLeft.stop();
                	motorRight.stop();
                	LCD.clearDisplay();
                  LCD.drawString("STOP", 0, 6);
                }
                
                else if(msg.getPayload().equals("stopPrg"))
                {
                	motorLeft.stop();
                	motorRight.stop();
                	LCD.clearDisplay();
                	LCD.drawString("PROGRAM STOPPED", 0, 6);
                	stop();
                }
                else if(msg.getPayload().startsWith("input"))
                {
                	String inputString = msg.getPayload().toString();
                	LCD.clearDisplay();
                	LCD.drawString("1" + inputString, 0, 1);
                	String inputType = inputString.substring(inputString.indexOf(" ") + 1);
                	//LCD.clearDisplay();
                	LCD.drawString("2" + inputType, 0, 2);
                	if(inputType.startsWith("move"))
                	{
                		String moveOrder = inputType.substring(inputType.indexOf(" ") + 1);
//                		LCD.clearDisplay();
                		LCD.drawString("3" + moveOrder, 0, 3);
                		LCD.drawString("4" + moveOrder.substring(0, moveOrder.indexOf(" ")), 0, 4);
                		LCD.drawString("5" + moveOrder.substring(moveOrder.indexOf(" ") + 1), 0, 5);
                		Double moveLeft = Double.parseDouble(((moveOrder.substring(0, moveOrder.indexOf(" "))))) * 100;
                		Double moveRight = Double.parseDouble((moveOrder.substring(moveOrder.indexOf(" ") + 1))) * 100;
//                		Double moveLeft = 0.50 * 100;
//                		Double moveRight = -0.234 * 100;
                		motorLeft.setPower(Math.abs(moveLeft.intValue()));
                		motorRight.setPower(Math.abs(moveRight.intValue()));
                		LCD.clearDisplay();
                      	LCD.drawString("MVL: " + moveLeft, 0, 5);
                      	LCD.drawString("MVR: " + Math.abs(moveRight.intValue()), 0, 6);
                		if(moveLeft > 0)
                			motorLeft.forward();
                		else if (moveLeft < 0)
                			motorLeft.backward();
                		else
                			motorLeft.flt();
                		if(moveRight > 0)
                			motorRight.forward();
                		else if (moveRight < 0)
                			motorRight.backward();
                		else
                			motorRight.flt();
                	} 
                	else if(inputType.startsWith("fire"))
                	{
                		Sound.buzz();
                	}
                	else
                	{
                		LCD.drawString("No match", 0, 5);
                	}
                  	//LCD.clearDisplay();
//                  	LCD.drawString("Input order received: ", 0, 1);
                }
                else {
                        LCD.drawString("No match", 0, 1);
                }
                
        } 
} 
