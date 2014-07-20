package orwell.proxy;

import java.io.FileNotFoundException;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.zeromq.ZMQ;

import orwell.proxy.IRobot.EnumConnectionState;
import orwell.proxy.config.ConfigProxy;
import orwell.proxy.config.ConfigRobots;
import orwell.proxy.config.ConfigServerGame;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.Configuration;

public class ProxyRobots {
	private ConfigServerGame configServerGame;
	private ConfigRobots configRobots;
	private ZMQ.Context context;
	private ZMQ.Socket sender;
	private ZMQ.Socket receiver;
	private HashMap<String,IRobot> tanksInitializedMap = new HashMap<String,IRobot>();
	private HashMap<String,IRobot> tanksConnectedMap = new HashMap<String,IRobot>();
	private HashMap<String,IRobot> tanksRegisteredMap = new HashMap<String,IRobot>();

	public ProxyRobots(String ConfigFileAddress, String serverGame) {
		Configuration configuration = new Configuration(ConfigFileAddress);
		try {
			// TODO Include populate into default constructor
			configuration.populate();
		} catch (FileNotFoundException | JAXBException e1) {
			e1.toString();
		}
		ConfigModel configProxyModel = configuration.getConfigModel();
		ConfigProxy configProxy = configProxyModel.getConfigProxy();
		configRobots = configuration.getConfigModel().getConfigRobots();
		try {
			configServerGame = configProxy.getConfigServerGame(serverGame);
		} catch (Exception e) {
			e.printStackTrace();
		}

		context = ZMQ.context(1);
		sender = context.socket(ZMQ.PUSH);
		receiver = context.socket(ZMQ.SUB);
		sender.setLinger(configProxy.getSenderLinger());
		receiver.setLinger(configProxy.getReceiverLinger());
	}

	public HashMap<String, IRobot> getTanksInitializedMap() {
		return tanksInitializedMap;
	}

	public HashMap<String, IRobot> getTanksConnectedMap() {
		return tanksConnectedMap;
	}

	public HashMap<String, IRobot> getTanksRegisteredMap() {
		return tanksRegisteredMap;
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
	
	/*
	 * This instantiate Tanks objects from a configuration
	 * It only set up the tanksInitializedMap
	 */
	public void initialiseTanks() {
		for (ConfigTank configTank: configRobots.getConfigRobotsToRegister()) {
			Camera camera = new Camera(configTank.getConfigCamera().getIp(),
					configTank.getConfigCamera().getPort());
			Tank tank = new Tank(configTank.getBluetoothName(),
					configTank.getBluetoothID(), camera);
			System.out.println(" NININININ " + configTank.getTempRoutingID());
			tank.setRoutingID(configTank.getTempRoutingID());
			this.tanksInitializedMap.put(tank.getRoutingID(), tank);
		}
		
		System.out.println("All " + this.tanksInitializedMap.size() + " tank(s) initialised");
	}
	
	/*
	 * This instantiate Tanks objects
	 * It only set up the tanksInitializedMap from another map
	 * @param map of tanks to setup
	 */
	public void initialiseTanks(HashMap<String, Tank> tanksToInitializeMap) {
		for (java.util.Map.Entry<String, Tank> entry: tanksToInitializeMap.entrySet()) {
			String routingID = entry.getKey();
			Tank tank = entry.getValue();
			this.tanksInitializedMap.put(routingID, tank);
		}
	}
	
	/*
	 * @param HashMap of tanks to connect to the proxy
	 */
	public void connectToRobots(HashMap<String, IRobot> tanksToConnectMap) {
		for (java.util.Map.Entry<String, IRobot> entry: tanksToConnectMap.entrySet()) {
			String routingID = entry.getKey();
			IRobot tank = entry.getValue();
			System.out.println("Connecting to robot: \n" + tank.toString());
			tank.connectToRobot();
			
			if(tank.getConnectionState() == EnumConnectionState.CONNECTION_FAILED) {
				System.out.println("Robot [" + tank.getRoutingID() + "] failed to connect to the proxy!");
			} else {
				// We only register Robots we manage to connect to the proxy
				this.sender.send(tank.getZMQRegister(), 0);
				System.out.println("Robot [" + tank.getRoutingID() + "] is connected to the proxy!");
				this.tanksConnectedMap.put(routingID, tank);
			}
		}
	}
	
	/*
	 * @param HashMap of tanks to register on the server-game
	 *        It is probably better to only register connected tanks
	 */
	public void registerRobots(HashMap<String, IRobot> tanksToRegisterMap) {
		for (IRobot tank: tanksToRegisterMap.values()) {
			this.sender.send(tank.getZMQRegister(), 0);
			System.out.println("Robot [" + tank.getRoutingID() + "] is connected to the proxy!");
		}
	}
	
	public void startCommunication() {
		String zmq_previousMessage = new String();
		String previousInput = new String();
		ZmqMessageWrapper zmqMessage;
		
		while (!Thread.currentThread().isInterrupted()) {
			byte[] raw_zmq_message = this.receiver.recv();
			zmqMessage = new ZmqMessageWrapper(raw_zmq_message);

			// We do not want to uselessly flood the robot
			if (zmqMessage.zmqMessageString.compareTo(zmq_previousMessage) == 0) {
				System.out
						.println("=======================================================================");
				continue;
			}
			
			switch (zmqMessage.type) {
			case "Registered":
				System.out.println("Setting ServerGame Registered to tank");
				if(this.tanksConnectedMap.containsKey(zmqMessage.routingId)) {
					IRobot registeredRobot = this.tanksConnectedMap.get(zmqMessage.routingId);
					this.tanksRegisteredMap.put(zmqMessage.routingId, registeredRobot);
					this.tanksConnectedMap.remove(zmqMessage.routingId);
					registeredRobot.setRegistered(zmqMessage.message);
					System.out.println(registeredRobot.serverGameRegisteredToString());
				} else {
					System.out.println("RoutingID " + zmqMessage.routingId + " is not an ID of a tank to register"); 
				}
				break;
			case "Input":
				System.out.println("Setting controller Input to tank");
				if (previousInput.compareTo(zmqMessage.zmqMessageString) == 0) {
					System.out
							.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					continue;
				}
				previousInput = zmqMessage.zmqMessageString;
				if(this.tanksRegisteredMap.containsKey(zmqMessage.routingId)) {
					IRobot tankTargeted = this.tanksRegisteredMap.get(zmqMessage.routingId);
					tankTargeted.setControllerInput(zmqMessage.message);
					System.out.println(tankTargeted.controllerInputToString());
				} else {
					System.out.println("RoutingID " + zmqMessage.routingId + " is not an ID of a tank to register"); 
				}
				break;
			case "GameState":
				break;
			default:
				System.out.println("[WARNING] Invalid Message type");
			}

			zmq_previousMessage = zmqMessage.zmqMessageString;

		}
	}

	public void stopCommunication() {
		this.sender.close();
		this.receiver.close();
		this.context.term();
	}
	
	public static void main(String[] args) throws Exception {
		ProxyRobots proxyRobots = new ProxyRobots("orwell/proxy/config/configuration.xml", "irondamien");
		proxyRobots.connectToServer();
		proxyRobots.initialiseTanks();
		proxyRobots.connectToRobots(proxyRobots.tanksInitializedMap);
		proxyRobots.registerRobots(proxyRobots.tanksConnectedMap);
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
