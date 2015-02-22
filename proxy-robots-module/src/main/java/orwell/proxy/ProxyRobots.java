package orwell.proxy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import orwell.proxy.IRobot.EnumConnectionState;
import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.ConfigProxy;
import orwell.proxy.config.ConfigRobots;
import orwell.proxy.config.ConfigServerGame;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.Configuration;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class ProxyRobots {
	final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class); 

	private ConfigServerGame configServerGame;
	private ConfigRobots configRobots;
	private ZMQ.Context context;
	private ZMQ.Socket sender;
	private ZMQ.Socket receiver;
	private HashMap<String, IRobot> tanksInitializedMap = new HashMap<String, IRobot>();
	private HashMap<String, IRobot> tanksConnectedMap = new HashMap<String, IRobot>();
	private HashMap<String, IRobot> tanksRegisteredMap = new HashMap<String, IRobot>();

	public ProxyRobots(String ConfigFileAddress, String serverGame,
			ZMQ.Context zmqContext) {
		logback.info("Constructor -- IN");
		Configuration configuration = new Configuration(ConfigFileAddress);
		try {
			// TODO Include populate into default constructor
			configuration.populate();
		} catch (JAXBException e1) {
			logback.error(e1.toString());
		}
		ConfigModel configProxyModel = configuration.getConfigModel();
		ConfigProxy configProxy = configProxyModel.getConfigProxy();
		configRobots = configuration.getConfigModel().getConfigRobots();
		try {
			configServerGame = configProxy.getConfigServerGame(serverGame);
		} catch (Exception e) {
			e.printStackTrace();
		}

		context = zmqContext;
		sender = context.socket(ZMQ.PUSH);
		receiver = context.socket(ZMQ.SUB);
		sender.setLinger(configProxy.getSenderLinger());
		receiver.setLinger(configProxy.getReceiverLinger());
		logback.info("Constructor -- OUT");
	}

	public ProxyRobots(String ConfigFileAddress, String serverGame) {
		this(ConfigFileAddress, serverGame, ZMQ.context(1));
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
		logback.info("ProxyRobots Sender created");
		receiver.connect("tcp://" + configServerGame.getIp() + ":"
				+ configServerGame.getSubPort());
		logback.info("ProxyRobots Receiver created");
		receiver.subscribe(new String("").getBytes());
	}

	/*
	 * This instantiate Tanks objects from a configuration It only set up the
	 * tanksInitializedMap
	 */
	public void initializeTanks() {
		for (ConfigTank configTank : configRobots.getConfigRobotsToRegister()) {
			Camera camera = new Camera(configTank.getConfigCamera().getIp(),
					configTank.getConfigCamera().getPort());
			//TODO Improve initialization of setImage to get something meaningful
			//from the string (like an actual picture)
			Tank tank = new Tank(configTank.getBluetoothName(),
					configTank.getBluetoothID(), camera, configTank.getImage());
			logback.info("NININININ " + configTank.getTempRoutingID());
			tank.setRoutingID(configTank.getTempRoutingID());
			this.tanksInitializedMap.put(tank.getRoutingID(), tank);
		}

		logback.info("All " + this.tanksInitializedMap.size()
				+ " tank(s) initialized");
	}

	/*
	 * This instantiate Tanks objects It only set up the tanksInitializedMap
	 * from another map
	 * 
	 * @param map of tanks to setup
	 */
	public void initializeTanks(HashMap<String, Tank> tanksToInitializeMap) {
		for (Map.Entry<String, Tank> entry : tanksToInitializeMap.entrySet()) {
			String routingID = entry.getKey();
			Tank tank = entry.getValue();
			tank.setRoutingID(routingID);
			this.tanksInitializedMap.put(routingID, tank);
		}
	}

	public void connectToRobots() {
		Iterator<Map.Entry<String, IRobot>> iterator = tanksInitializedMap
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, IRobot> entry = iterator.next();
			String routingID = entry.getKey();
			IRobot tank = entry.getValue();
			logback.info("Connecting to robot: \n" + tank.toString());
			tank.connectToRobot();

			if (tank.getConnectionState() == EnumConnectionState.CONNECTION_FAILED) {
				logback.info("Robot [" + tank.getRoutingID()
						+ "] failed to connect to the proxy!");
			} else {
				logback.info("Robot [" + tank.getRoutingID()
						+ "] is connected to the proxy!");
				this.tanksConnectedMap.put(routingID, tank);
				iterator.remove();
			}
		}
	}

	public void registerRobots() {
		for (IRobot tank : tanksConnectedMap.values()) {
			tank.buildRegister();
			sender.send(tank.getZmqRegister(), 0);
			logback.info("TEST RegisterHeader: " + Arrays.toString(tank.getZmqRegister()));
			logback.info("Robot [" + tank.getRoutingID()
					+ "] is trying to register itself to the server!");
		}
	}

	private void updateConnectedTanks() {
        for (IRobot tank : tanksConnectedMap.values()) {
			if(tank.getConnectionState() != EnumConnectionState.CONNECTED){
				tanksConnectedMap.remove(tank.getRoutingID());
				logback.info("Removing dead tank connected: " + tank.getRoutingID());
				if(this.tanksRegisteredMap.containsKey(tank.getRoutingID())){
					tanksRegisteredMap.remove(tank.getRoutingID());
					logback.info("Removing dead tank registered: " + tank.getRoutingID());
				}
			}
		}
	}

    public void sendServerRobotState() {
        for (IRobot tank : tanksRegisteredMap.values()) {
            byte[] zmqServerRobotState = tank.getAndClearZmqServerRobotState();
            if(null == zmqServerRobotState) {
                continue;
            } else {
//                logback.debug("TEST ServerRobotStateHeader: " + Arrays.toString(zmqServerRobotState));
//                logback.debug("Robot [" + tank.getRoutingID()
//                        + "] is sending its ServerRobotState to the server!");
                this.sender.send(zmqServerRobotState, 0);
            }
        }
    }

	public void startCommunication(ZmqMessageWrapper interruptMessage) {
        String zmq_previousMessage = new String();
        String previousInput = new String();
        ZmqMessageWrapper zmqMessage;
        boolean interruptMessageReceived = false;
        while (!Thread.currentThread().isInterrupted()
                && !tanksConnectedMap.isEmpty()
                && !interruptMessageReceived) {
            byte[] raw_zmq_message = this.receiver.recv(ZMQ.NOBLOCK);
            if (null != raw_zmq_message) {
                zmqMessage = new ZmqMessageWrapper(raw_zmq_message);

                // We do not want to uselessly flood the robot
                if (zmqMessage.zmqMessageString.compareTo(zmq_previousMessage) == 0) {
                    logback.debug("Current zmq message identical to previous zmq message");
                } else {
                    switch (zmqMessage.type) {
                        case "Registered":
                            logback.info("Setting ServerGame Registered to tank");
                            if (this.tanksConnectedMap.containsKey(zmqMessage.routingId)) {

                                IRobot registeredRobot = this.tanksConnectedMap
                                        .get(zmqMessage.routingId);
                                registeredRobot.setRegistered(zmqMessage.message);
                                this.tanksRegisteredMap.put(registeredRobot.getRoutingID(),
                                        registeredRobot);
                                logback.info("Registered robot : " + registeredRobot
                                        .serverGameRegisteredToString());
                            } else {
                                logback.info("RoutingID " + zmqMessage.routingId
                                        + " is not an ID of a tank to register");
                            }
                            break;
                        case "Input":
                            logback.info("Setting controller Input to tank");
                            if (previousInput.compareTo(zmqMessage.zmqMessageString) == 0) {
                                logback.debug("Current input identical to previous input");
                            } else {
                                previousInput = zmqMessage.zmqMessageString;
                                if (this.tanksRegisteredMap.containsKey(zmqMessage.routingId)) {
                                    IRobot tankTargeted = this.tanksRegisteredMap
                                            .get(zmqMessage.routingId);
                                    tankTargeted.setControllerInput(zmqMessage.message);
                                    logback.info("tankTargeted input : " + tankTargeted.controllerInputToString());
                                } else {
                                    logback.info("RoutingID " + zmqMessage.routingId
                                            + " is not an ID of a registered tank");
                                }
                            }
                            break;
                        case "GameState":
                            logback.info("[WARNING] Received GameState - not handled");
                            break;
                        default:
                            logback.info("[WARNING] Invalid Message type");
                    }
                }
                zmq_previousMessage = zmqMessage.zmqMessageString;

                logback.debug("zmqMessage.type = " + zmqMessage.type);

                if (null != interruptMessage && zmqMessage.type.equals(interruptMessage.type)) {
                    logback.info("Communication interrupted with interrupt message : " + interruptMessage.type);
                    interruptMessageReceived = true;
                }
            }
            sendServerRobotState();
			updateConnectedTanks();
		}
		logback.info("End of communication");
	}

	public void stopCommunication() {
		logback.info("Stopping communication");
		sender.close();
		receiver.close();
		context.term();
		logback.info("Communication stopped");
	}
	
	public void printLoggerState() {
		// print internal state
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
	}

	public static void main(String[] args) throws Exception {
		ProxyRobots proxyRobots = new ProxyRobots(
				"/configuration.xml", "platypus");
		proxyRobots.connectToServer();
		proxyRobots.initializeTanks();
		proxyRobots.connectToRobots();
		proxyRobots.registerRobots();
		proxyRobots.startCommunication(null);

		// proxyRobots.sender.send(proxyRobots.tank.getZMQRobotState(), 0);
		// logback.info("Message sent");
		//
		// String request = "Banana";
		// proxyRobots.sender.send(request, 0);
		// logback.info("Message sent: " + request);
		proxyRobots.stopCommunication();
	}
}
