package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orwell.proxy.config.*;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class ProxyRobots implements IZmqMessageListener {
	final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class); 

	private IConfigServerGame configServerGame;
	private IConfigRobots configRobots;
    protected IMessageFramework mfProxy;
	protected IRobotsMap robotsMap;

    protected CommunicationService communicationService = new CommunicationService();
    private Thread communicationThread = new Thread(communicationService);

	public ProxyRobots(IMessageFramework mfProxy,
                       IConfigServerGame configServerGame,
                       IConfigRobots configRobots,
                       IRobotsMap robotsMap) {
		logback.info("Constructor -- IN");
        assert(null != mfProxy);
        assert(null != configServerGame);
        assert(null != configRobots);
        assert(null != robotsMap);

        this.mfProxy = mfProxy;
        this.configServerGame = configServerGame;
        this.configRobots = configRobots;
        this.robotsMap = robotsMap;

        mfProxy.addZmqMessageListener(this);
		logback.info("Constructor -- OUT");
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
	public void initializeTanksFromConfig() {
		for (ConfigTank configTank : configRobots.getConfigRobotsToRegister()) {
			Camera camera = new Camera(configTank.getConfigCamera().getIp(),
					configTank.getConfigCamera().getPort());
			//TODO Improve initialization of setImage to get something meaningful
			//from the string (like an actual picture)
			Tank tank = new Tank(configTank.getBluetoothName(),
					configTank.getBluetoothID(), camera, configTank.getImage());
			logback.info("Temporary routing ID: " + configTank.getTempRoutingID());
			tank.setRoutingID(configTank.getTempRoutingID());
			this.robotsMap.add(tank);
		}

		logback.info("All " + this.robotsMap.getRobotsArray().size()
				+ " tank(s) initialized");
	}


	public void connectToRobots() {
        for (IRobot robot : robotsMap.getNotConnectedRobots()) {
            robot.connectToDevice();
        }
	}


	public void sendRegister() {
		for (IRobot robot : robotsMap.getConnectedRobots()) {
			robot.buildRegister();
			mfProxy.sendZmqMessage(EnumMessageType.REGISTER, robot.getRoutingID(),
                    robot.getRegisterBytes());
			logback.info("Robot [" + robot.getRoutingID()
					+ "] is trying to register itself to the server!");
		}
	}


    public void sendServerRobotStates() {
        for (IRobot robot : robotsMap.getRegisteredRobots()) {
            byte[] zmqServerRobotState = robot.getAndClearZmqServerRobotStateBytes();
            if(null == zmqServerRobotState) {
                continue;
            } else {
                mfProxy.sendZmqMessage(EnumMessageType.SERVER_ROBOT_STATE,
                        robot.getRoutingID(), zmqServerRobotState);
            }
        }
    }

	public void startCommunicationService() {
        communicationThread.start();
	}

	public void closeCommunicationService() {
        disconnectAllTanks();
	}

    private void disconnectAllTanks() {
        for (IRobot tank : robotsMap.getConnectedRobots()) {
            tank.closeConnection();
        }
    }

    private void onRegistered(ZmqMessageWrapper zmqMessage) {
        logback.info("Setting ServerGame Registered to tank");
        String routingId = zmqMessage.getRoutingId();
        if (robotsMap.isRobotConnected(routingId)) {
            IRobot registeredRobot = robotsMap.get(routingId);
            registeredRobot.setRegistered(zmqMessage.getMessageBytes());
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a tank to register");
        }
    }

    private void onInput(ZmqMessageWrapper zmqMessage) {
        logback.info("Setting controller Input to tank");
        String routingId = zmqMessage.getRoutingId();
        if (robotsMap.isRobotRegistered(routingId)) {
            IRobot robotTargeted = robotsMap.get(routingId);
            robotTargeted.setControllerInput(zmqMessage.getMessageBytes());
            logback.info("tankTargeted input : " + robotTargeted.controllerInputToString());
        } else {
            logback.info("RoutingID " + routingId
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
		ProxyRobots proxyRobots = new ProxyRobotsFactory("/configuration.xml", "platypus").getProxyRobots();

		proxyRobots.connectToServer();
		proxyRobots.initializeTanksFromConfig();
		proxyRobots.connectToRobots();
		proxyRobots.sendRegister();
		proxyRobots.startCommunicationService();
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

    class CommunicationService implements Runnable {
        public void run(){
            while (!Thread.currentThread().isInterrupted() &&
                    !robotsMap.getConnectedRobots().isEmpty()) {
                sendServerRobotStates();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logback.error("CommunicationService thread sleep exception: " + e.getMessage());
                }
            }
            logback.info("End of communication service");
            mfProxy.close();
            Thread.yield();
        }
    }
}
