package orwell.proxy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orwell.proxy.IRobot.EnumConnectionState;
import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.ConfigProxy;
import orwell.proxy.config.ConfigRobots;
import orwell.proxy.config.ConfigServerGame;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.Configuration;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class ProxyRobots implements IZmqMessageListener {
	final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class); 

	private ConfigServerGame configServerGame;
	private ConfigRobots configRobots;
    private ZmqMessageFramework mfProxy;
	private HashMap<String, IRobot> tanksInitializedMap = new HashMap<String, IRobot>();
	private HashMap<String, IRobot> tanksConnectedMap = new HashMap<String, IRobot>();
	private HashMap<String, IRobot> tanksRegisteredMap = new HashMap<String, IRobot>();

	public ProxyRobots(String ConfigFileAddress, String serverGame) {
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

        mfProxy = new ZmqMessageFramework(
                configProxy.getSenderLinger(),
                configProxy.getReceiverLinger());
        mfProxy.addZmqMessageListener(this);
		logback.info("Constructor -- OUT");
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
		mfProxy.connectToServer(
                configServerGame.getIp(),
                configServerGame.getPushPort(),
                configServerGame.getSubPort());
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
			logback.info("Temporary routing ID: " + configTank.getTempRoutingID());
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
			mfProxy.sendZmqMessage(EnumMessageType.REGISTER, tank.getRoutingID(),
                    tank.getRegisterBytes());
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
        for (IRobot tank : tanksConnectedMap.values()) {
            byte[] zmqServerRobotState = tank.getAndClearZmqServerRobotStateBytes();
            if(null == zmqServerRobotState) {
                continue;
            } else {
//                logback.debug("TEST ServerRobotStateHeader: " + Arrays.toString(zmqServerRobotState));
//                logback.debug("Robot [" + tank.getRoutingID()
//                        + "] is sending its ServerRobotState to the server!");
                mfProxy.sendZmqMessage(EnumMessageType.SERVER_ROBOT_STATE,
                        tank.getRoutingID(), zmqServerRobotState);
            }
        }
    }

	public void startCommunication() {
        String zmq_previousMessage = new String();
        String previousInput = new String();
        ZmqMessageWrapper zmqMessage;
        while (!Thread.currentThread().isInterrupted()
                && !tanksConnectedMap.isEmpty()) {
			updateConnectedTanks();
            sendServerRobotState();
		}
		logback.info("End of communication");
	}

	public void stopCommunication() {
		mfProxy.close();
	}

    private void onRegistered(ZmqMessageWrapper zmqMessage) {
        logback.info("Setting ServerGame Registered to tank");
        if (this.tanksConnectedMap.containsKey(zmqMessage.getRoutingId())) {
            IRobot registeredRobot = this.tanksConnectedMap
                    .get(zmqMessage.getRoutingId());
            registeredRobot.setRegistered(zmqMessage.getMessageBytes());
            this.tanksRegisteredMap.put(registeredRobot.getRoutingID(),
                    registeredRobot);
            logback.info("Registered robot : " + registeredRobot
                    .serverGameRegisteredToString());
        } else {
            logback.info("RoutingID " + zmqMessage.getRoutingId()
                    + " is not an ID of a tank to register");
        }
    }

    private void onInput(ZmqMessageWrapper zmqMessage) {
        logback.info("Setting controller Input to tank");
        if (this.tanksRegisteredMap.containsKey(zmqMessage.getRoutingId())) {
            IRobot tankTargeted = this.tanksRegisteredMap
                    .get(zmqMessage.getRoutingId());
            tankTargeted.setControllerInput(zmqMessage.getMessageBytes());
            logback.info("tankTargeted input : " + tankTargeted.controllerInputToString());
        } else {
            logback.info("RoutingID " + zmqMessage.getRoutingId()
                    + " is not an ID of a registered tank");
        }
    }

    private void onGameState(ZmqMessageWrapper zmqMessage) {
        logback.warn("Received GameState - not handled");
    }

    private void onDefault() {
        logback.warn("Unknown message type");
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
		proxyRobots.startCommunication();

		// proxyRobots.sender.send(proxyRobots.tank.getZMQRobotState(), 0);
		// logback.info("Message sent");
		//
		// String request = "Banana";
		// proxyRobots.sender.send(request, 0);
		// logback.info("Message sent: " + request);
		proxyRobots.stopCommunication();
	}

    @Override
    public void receivedNewZmq(ZmqMessageWrapper msg) {
        switch (msg.getMessageType()) {
            case REGISTERED:
                onRegistered(msg);
                break;
            case INPUT:
                onInput(msg);
                break;
            case GAMESTATE:
                onGameState(msg);
                break;
            default:
                onDefault();
        }
    }
}
