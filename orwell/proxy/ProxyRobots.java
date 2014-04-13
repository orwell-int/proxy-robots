package orwell.proxy;

import org.zeromq.ZMQ;


import orwell.messages.Robot;


public class ProxyRobots
{
	public ProxyRobots()
	{
	}
	
/*	static Robot.RobotState buildTestRobot()
	{
		Robot.RobotState.Builder testRobot = Robot.RobotState.newBuilder();
		Robot.RobotState.Move.Builder testMove = Robot.RobotState.Move.newBuilder();
		testMove.setLeft(42);
		testMove.setRight(99);
		testRobot.setMove(testMove.build());

		testRobot.setActive(true);
		testRobot.setLife(42);
		
		return testRobot.build();
	}*/

	public static void main(String[] args) throws Exception
	{
	    ZMQ.Context context = ZMQ.context(1);
	    ZMQ.Socket sender = context.socket(ZMQ.PUSH);
	    ZMQ.Socket receiver = context.socket(ZMQ.SUB);
	    sender.setLinger(1000);
	    receiver.setLinger(1000);
	    String serverAddress = "192.168.1.37";
	    sender.connect("tcp://" + serverAddress + ":9000");
	    System.out.println("ProxyRobots Sender created");
	    receiver.connect("tcp://" + serverAddress + ":9001");
	    System.out.println("ProxyRobots Receiver created");
	    receiver.subscribe(new String("").getBytes());

	    //        socket.bind("tcp://*:5555");
        
	    Tank tank = new Tank("Daneel", "001653119482");
	    tank.setNetworkID("BananaOne");
	    System.out.println("Building robotState for test: \n" + tank.toString());
	    tank.connectToNXT();
	    
//	    Robot.RobotState robotState = buildTestRobot();
//        System.out.println("Building robotState for test");
//        ProxyRobots.Print(robotState);


        sender.send(tank.getZMQRobotState(), 0);
        System.out.println("Message sent");
	
	    String request = "Banana";
	    sender.send(request, 0);
	    System.out.println("Message sent: " + request);

	    byte space = 32; // ascii code of SPACE character
	    
	    String zmq_previousMessage = new String();
	    String previousInput = new String();
	    
        while (!Thread.currentThread().isInterrupted())
	    {
        	byte [] raw_zmq_message = receiver.recv();
        	String zmq_message = new String(raw_zmq_message);
        	
        	// We do not want to uselessly flood the robot
        	if(zmq_message.compareTo(zmq_previousMessage) == 0)
        	{
        		System.out.println("=======================================================================");
        		continue;
        	}
			int indexType = 0;
			int indexMessage = 0;
			int index = 0;
			for (byte item: raw_zmq_message)
			{
				if (0 == indexType)
				{
					if (space == item)
					{
						indexType = index + 1;
					}
				}
				else
				{
					if (space == item)
					{
						indexMessage = index + 1;
						break;
					}
				}
				++index;
			}
			// routingID type message
			//           ^    ^
			//           |    indexMessage
			//           indexType
			int lengthRoutingID = indexType - 1;
			int lengthType = indexMessage - indexType - 1;
			String routingID = new String(raw_zmq_message, 0, lengthRoutingID);
			String type = new String(raw_zmq_message, indexType, lengthType);
			int lengthMessage = raw_zmq_message.length - lengthType - lengthRoutingID - 2;
			byte [] message = new byte[lengthMessage];
			System.arraycopy(raw_zmq_message, indexMessage, message, 0, message.length);
		
			System.out.flush();
			System.out.println("Message received: " + zmq_message);
//			Pattern messagePattern = Pattern.compile("([^ ]*) ([^ ]*) (.*)");

//			System.out.println("Message [DEST]: " + routingID);
			tank.setNetworkID(routingID);
			System.out.println("Message [TYPE]: " + type);
//			System.out.println("Message [MSG] : " + new String(message));
			
			switch (type)
			{
				case "Hello":	
					System.out.println("Setting controller Hello to tank");
					tank.setControllerHello(message);
					System.out.println(tank.controllerHelloToString());
					break;
				case "Input":	
					System.out.println("Setting controller Input to tank");
					if(previousInput.compareTo(zmq_message) == 0)
					{
		        		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						continue;
					}
					previousInput = zmq_message;
					tank.setControllerInput(message);
//					System.out.println("length: " + message.length);
					System.out.println(tank.controllerInputToString());
					break;
				case "GameState":
					break;
				default:		
					System.out.println("[WARNING] Invalid Message type");
			}
			
			zmq_previousMessage = zmq_message;

	    }
	    sender.close();
	    receiver.close();
	    context.term();
	}
}
