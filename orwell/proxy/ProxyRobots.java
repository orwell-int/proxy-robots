package orwell.proxy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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
	private ConfigServerGame configServerGame;
	private ConfigRobots configRobots;
	private ZMQ.Context context;
	private ZMQ.Socket sender;
	private ZMQ.Socket receiver;
	private HashMap<String,Tank> tanksToRegisterMap = new HashMap<String,Tank>();
	private HashMap<String,Tank> registeredTanksMap = new HashMap<String,Tank>();

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
	 * This instantiate Tanks objects
	 * Then, it tries to connect to the tanks themselves using BT
	 * and register each of them (connected) on the server-game
	 */
	public void initialiseTanks() {
		boolean isConnected;
		for (ConfigTank configTank: configRobots.getConfigRobotsToRegister()) {
			isConnected = false;
			Camera camera = new Camera(configTank.getConfigCamera().getIp(),
					configTank.getConfigCamera().getPort());
			Tank tank = new Tank(configTank.getBluetoothName(),
					configTank.getBluetoothID(), camera);
			System.out.println(" NININININ " + configTank.getTempRoutingID());
			tank.setRoutingID(configTank.getTempRoutingID());
			tanksToRegisterMap.put(tank.getRoutingID(),tank);
			System.out.println("Connecting to robot: \n" + tank.toString());
			isConnected = tank.connectToRobot();

			
			if(!isConnected) {
				System.out.println("Tank [" + tank.getRoutingID() + "] failed to connect to the proxy!");
			} else {
				// We only register Tanks we manage to connect to the proxy
				this.sender.send(tank.getZMQRegister(), 0);
				System.out.println("Tank [" + tank.getRoutingID() + "] is connected to the proxy!");
			}
		}
			
		System.out.println("All tanks initialised");
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
			case "Hello":
				System.out.println("Setting controller Hello to tank");
				if(this.registeredTanksMap.containsKey(zmqMessage.routingId)) {
					Tank tankTargeted = this.registeredTanksMap.get(zmqMessage.routingId);
					tankTargeted.setControllerHello(zmqMessage.message);
					System.out.println(tankTargeted.controllerHelloToString());
				} else {
					System.out.println("RoutingID " + zmqMessage.routingId + " is not an ID of a tank to register"); 
				}
				break;
			case "Registered":
				System.out.println("Setting ServerGame Registered to tank");
				if(this.tanksToRegisterMap.containsKey(zmqMessage.routingId)) {
					Tank registeredTank = this.tanksToRegisterMap.get(zmqMessage.routingId);
					this.registeredTanksMap.put(zmqMessage.routingId, registeredTank);
					this.tanksToRegisterMap.remove(zmqMessage.routingId);
					registeredTank.setRegistered(zmqMessage.message);
					System.out.println(registeredTank.serverGameRegisteredToString());
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
				if(this.registeredTanksMap.containsKey(zmqMessage.routingId)) {
					Tank tankTargeted = this.registeredTanksMap.get(zmqMessage.routingId);
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
