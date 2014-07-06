package orwell.proxy;

import java.io.FileNotFoundException;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.zeromq.ZMQ;

import orwell.proxy.config.ConfigProxy;
import orwell.proxy.config.ConfigRobots;
import orwell.proxy.config.ConfigServerGame;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.Configuration;

public class ProxyRobots {
	private static final String CONFIGURATION_FILE = "orwell/proxy/config/configuration.xml";
	private static final String SERVER_GAME = "irondamien";
	private static final String TANK_NAME = "BananaOne";
	private ConfigProxy configProxy;
	private ConfigServerGame configServerGame;
	private ConfigRobots configRobots;
	private ZMQ.Context context;
	private ZMQ.Socket sender;
	private ZMQ.Socket receiver;
	private HashMap<String,Tank> tanksToRegisterMap = new HashMap<String,Tank>();
	private HashMap<String,Tank> registeredTanksMap = new HashMap<String,Tank>();

	public ProxyRobots() {
		Configuration configuration = new Configuration(CONFIGURATION_FILE);
		try {
			// TODO Include populate into default constructor
			configuration.populate();
		} catch (FileNotFoundException | JAXBException e1) {
			e1.toString();
		}
		ConfigModel configProxyModel = configuration.getConfigModel();
		configProxy = configProxyModel.getConfigProxy();
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

	public void connectToServer() {
		sender.connect("tcp://" + configServerGame.getIp() + ":"
				+ configServerGame.getPushPort());
		System.out.println("ProxyRobots Sender created");
		receiver.connect("tcp://" + configServerGame.getIp() + ":"
				+ configServerGame.getSubPort());
		System.out.println("ProxyRobots Receiver created");
		receiver.subscribe(new String("").getBytes());
	}

	public void initialiseTanks() {
		boolean isConnected = false;
		try {
			ConfigTank configTank = configRobots.getConfigTank(TANK_NAME);
			Camera camera = new Camera(configTank.getConfigCamera().getIp(),
					configTank.getConfigCamera().getPort());
			Tank tank = new Tank(configTank.getBluetoothName(),
					configTank.getBluetoothID(), camera);
			System.out.println(" NININININ " + configTank.getTempRoutingID());
			tank.setRoutingID(configTank.getTempRoutingID());
			tanksToRegisterMap.put(tank.getRoutingID(),tank);
			System.out.println("Connecting to robot: \n" + tank.toString());
			isConnected = tank.connectToNXT();
			this.sender.send(tank.getZMQRegister(), 0);
		} catch (Exception e) {
			// getConfigTank has failed (tank not found in config file)
			e.printStackTrace();
		}
		if(!isConnected) {
			System.out.println("Tank is NOT connected!");
		} else {
			System.out.println("Tank is connected to the proxy!");
		}
		System.out.println("Tank initialised");
	}

	public void startCommunication() {
		byte space = 32; // ascii code of SPACE character

		String zmq_previousMessage = new String();
		String previousInput = new String();

		while (!Thread.currentThread().isInterrupted()) {
			byte[] raw_zmq_message = this.receiver.recv();
			String zmq_message = new String(raw_zmq_message);

			// We do not want to uselessly flood the robot
			if (zmq_message.compareTo(zmq_previousMessage) == 0) {
				System.out
						.println("=======================================================================");
				continue;
			}
			int indexType = 0;
			int indexMessage = 0;
			int index = 0;
			for (byte item : raw_zmq_message) {
				if (0 == indexType) {
					if (space == item) {
						indexType = index + 1;
					}
				} else {
					if (space == item) {
						indexMessage = index + 1;
						break;
					}
				}
				++index;
			}
			// routingID type message
			// ^ ^
			// | indexMessage
			// indexType
			int lengthRoutingID = indexType - 1;
			int lengthType = indexMessage - indexType - 1;
			String routingID = new String(raw_zmq_message, 0, lengthRoutingID);
			String type = new String(raw_zmq_message, indexType, lengthType);
			int lengthMessage = raw_zmq_message.length - lengthType
					- lengthRoutingID - 2;
			byte[] message = new byte[lengthMessage];
			System.arraycopy(raw_zmq_message, indexMessage, message, 0,
					message.length);

			System.out.flush();
			System.out.println("Message received: " + zmq_message);

			//proxyRobots.tank.setRoutingID(routingID);
			System.out.println("Message [TYPE]: " + type);

			switch (type) {
			case "Hello":
				System.out.println("Setting controller Hello to tank");
				if(this.registeredTanksMap.containsKey(routingID)) {
					Tank tankTargeted = this.registeredTanksMap.get(routingID);
					tankTargeted.setControllerHello(message);
					System.out.println(tankTargeted.controllerHelloToString());
				} else {
					System.out.println("RoutingID " + routingID + " is not an ID of a tank to register"); 
				}
				break;
			case "Registered":
				System.out.println("Setting ServerGame Registered to tank");
				if(this.tanksToRegisterMap.containsKey(routingID)) {
					Tank registeredTank = this.registeredTanksMap.get(routingID);
					this.registeredTanksMap.put(routingID, registeredTank);
					this.tanksToRegisterMap.remove(routingID);
					registeredTank.setRegistered(message);
					System.out.println(registeredTank.serverGameRegisteredToString());
				} else {
					System.out.println("RoutingID " + routingID + " is not an ID of a tank to register"); 
				}
				break;
			case "Input":
				System.out.println("Setting controller Input to tank");
				if (previousInput.compareTo(zmq_message) == 0) {
					System.out
							.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					continue;
				}
				previousInput = zmq_message;
				if(this.registeredTanksMap.containsKey(routingID)) {
					Tank tankTargeted = this.registeredTanksMap.get(routingID);
					tankTargeted.setControllerInput(message);
					System.out.println(tankTargeted.controllerInputToString());
				} else {
					System.out.println("RoutingID " + routingID + " is not an ID of a tank to register"); 
				}
				break;
			case "GameState":
				break;
			default:
				System.out.println("[WARNING] Invalid Message type");
			}

			zmq_previousMessage = zmq_message;

		}
	}

	public void stopCommunication() {
		this.sender.close();
		this.receiver.close();
		this.context.term();
	}
	
	public static void main(String[] args) throws Exception {
		ProxyRobots proxyRobots = new ProxyRobots();
		proxyRobots.connectToServer();
		proxyRobots.initialiseTanks();
		proxyRobots.startCommunication();

		// proxyRobots.sender.send(proxyRobots.tank.getZMQRobotState(), 0);
		// System.out.println("Message sent");
		//
//		 String request = "Banana";
//		 proxyRobots.sender.send(request, 0);
//		 System.out.println("Message sent: " + request);
		proxyRobots.stopCommunication();
	}
}
