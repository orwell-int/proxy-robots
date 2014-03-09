package orwell.proxy;

import org.zeromq.ZMQ;


import orwell.messages.Robot;


public class ProxyRobots
{
	public ProxyRobots()
	{
	}
	
	static Robot.RobotState buildTestRobot()
	{
		Robot.RobotState.Builder testRobot = Robot.RobotState.newBuilder();
		Robot.RobotState.Move.Builder testMove = Robot.RobotState.Move.newBuilder();
		testMove.setLeft(42);
		testMove.setRight(99);
		testRobot.setMove(testMove.build());

		testRobot.setActive(true);
		testRobot.setLife(42);
		
		return testRobot.build();
	}

	public static void main(String[] args) throws Exception
	{
	    ZMQ.Context context = ZMQ.context(1);
	    ZMQ.Socket sender = context.socket(ZMQ.PUSH);
	    ZMQ.Socket receiver = context.socket(ZMQ.SUB);
	    sender.setLinger(1000);
	    receiver.setLinger(1000);
	    String serverAddress = "192.168.1.46";
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
	    
	    byte [] raw_zmq_previousInput = null;
	    
        while (!Thread.currentThread().isInterrupted())
	    {
        	byte [] raw_zmq_input = receiver.recv();
        	
        	// We do not want to uselessly flood the robot
        	if(raw_zmq_input.toString().compareTo(raw_zmq_previousInput.toString()) == 0)
        	{
        		System.out.println("=======================================================================");
        		continue;
        	}
        	System.out.println("raw length: " + raw_zmq_input.length);
			int indexType = 0;
			int indexMessage = 0;
			int index = 0;
			for (byte item: raw_zmq_input)
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
			String routingID = new String(raw_zmq_input, 0, lengthRoutingID);
			String type = new String(raw_zmq_input, indexType, lengthType);
			int lengthMessage = raw_zmq_input.length - lengthType - lengthRoutingID - 2;
			byte [] message = new byte[lengthMessage];
			System.arraycopy(raw_zmq_input, indexMessage, message, 0, message.length);
		
			String zmq_input = new String(raw_zmq_input);
			System.out.println("length: " + zmq_input.getBytes().length);
			System.out.flush();
			System.out.println("Message received: " + zmq_input);
//			Pattern messagePattern = Pattern.compile("([^ ]*) ([^ ]*) (.*)");

			System.out.println("Message [DEST]: " + routingID);
			tank.setNetworkID(routingID);
			System.out.println("Message [TYPE]: " + type);
			System.out.println("Message [MSG] : " + new String(message));
			
			switch (type)
			{
				case "Hello":	
					System.out.println("Setting controller Hello to tank");
					tank.setControllerHello(message);
					System.out.println(tank.controllerHelloToString());
					break;
				case "Input":	
					System.out.println("Setting controller Input to tank");
					tank.setControllerInput(message);
					System.out.println("length: " + message.length);
					System.out.println(tank.controllerInputToString());
					break;
				default:		
					System.out.println("[WARNING] Invalid Message type");
			}
			
			raw_zmq_previousInput = raw_zmq_input;

	    }
	    sender.close();
	    receiver.close();
	    context.term();
	}
}
