package orwell.proxy;

import org.zeromq.ZMQ;

import orwell.proxy.config.ConfigProxy;
import orwell.proxy.config.ConfigRobots;
import orwell.proxy.config.ConfigServerGame;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.Configuration;

public class ProxyRobots
{
	private String CONFIGURATION_FILE = "orwell/proxy/config/configuration.xml";
	private ConfigProxy configProxy;
	private String SERVER_GAME = "platypus";
	private String TANK_NAME = "Daneel";
	private ConfigServerGame configServerGame;
	private ZMQ.Context context;
    private ZMQ.Socket sender;
    private ZMQ.Socket receiver;
    private Tank tank;
	private ConfigRobots configRobots;
	
	public ProxyRobots()
	{
		Configuration configuration = new Configuration(CONFIGURATION_FILE);
		configProxy = configuration.getConfigModel().getConfigProxy();
		configRobots = configuration.getConfigModel().getConfigRobots();
		try {
			configServerGame = configProxy.getConfigServerGame(SERVER_GAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    context = ZMQ.context(1);
	    sender = context.socket(ZMQ.PUSH);
	    receiver = context.socket(ZMQ.SUB);
	    sender.setLinger(configProxy.getSenderLinger());
	    receiver.setLinger(configProxy.getReceiverLinger());
	}
	
	public void connectToServer()
	{
	    sender.connect("tcp://" + configServerGame.getIp() + ":" + configServerGame.getPushPort());
	    System.out.println("ProxyRobots Sender created");
	    receiver.connect("tcp://" + configServerGame.getIp() + ":" + configServerGame.getSubPort());
	    System.out.println("ProxyRobots Receiver created");
	    receiver.subscribe(new String("").getBytes());
	}
	
	public void connectToTank()
	{
		try {
			ConfigTank configTank = configRobots.getConfigTank(TANK_NAME);
		    tank = new Tank(configTank.getBluetoothName(), configTank.getBluetoothID());
		    tank.setNetworkID(configTank.getNetworkID());
		    System.out.println("Connecting to robot: \n" + tank.toString());
		    tank.connectToNXT();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Connected to tank!");
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
		ProxyRobots proxyRobots = new ProxyRobots();
		proxyRobots.connectToServer();
		proxyRobots.connectToTank();
	    
		proxyRobots.sender.send(proxyRobots.tank.getZMQRobotState(), 0);
        System.out.println("Message sent");
	
	    String request = "Banana";
	    proxyRobots.sender.send(request, 0);
	    System.out.println("Message sent: " + request);

	    byte space = 32; // ascii code of SPACE character
	    
	    String zmq_previousMessage = new String();
	    String previousInput = new String();
	    
        while (!Thread.currentThread().isInterrupted())
	    {
        	byte [] raw_zmq_message = proxyRobots.receiver.recv();
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
			proxyRobots.tank.setNetworkID(routingID);
			System.out.println("Message [TYPE]: " + type);
//			System.out.println("Message [MSG] : " + new String(message));
			
			switch (type)
			{
				case "Hello":	
					System.out.println("Setting controller Hello to tank");
					proxyRobots.tank.setControllerHello(message);
					System.out.println(proxyRobots.tank.controllerHelloToString());
					break;
				case "Input":	
					System.out.println("Setting controller Input to tank");
					if(previousInput.compareTo(zmq_message) == 0)
					{
		        		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						continue;
					}
					previousInput = zmq_message;
					proxyRobots.tank.setControllerInput(message);
//					System.out.println("length: " + message.length);
					System.out.println(proxyRobots.tank.controllerInputToString());
					break;
				case "GameState":
					break;
				default:		
					System.out.println("[WARNING] Invalid Message type");
			}
			
			zmq_previousMessage = zmq_message;

	    }
        proxyRobots.sender.close();
        proxyRobots.receiver.close();
        proxyRobots.context.term();
	}
}
