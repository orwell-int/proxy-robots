package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigFactoryParameters;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.IConfigRobots;
import orwell.proxy.config.IConfigServerGame;
import orwell.proxy.robot.Camera;
import orwell.proxy.robot.IRobot;
import orwell.proxy.robot.IRobotsMap;
import orwell.proxy.robot.Tank;
import orwell.proxy.zmq.IZmqMessageBroker;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ZmqMessageBOM;

public class ProxyRobots implements IZmqMessageListener {
    private final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class);
    private final IConfigServerGame configServerGame;
    private final IConfigRobots configRobots;
    private final IZmqMessageBroker mfProxy;
    protected IRobotsMap robotsMap;
    private final CommunicationService communicationService = new CommunicationService();
    private final Thread communicationThread = new Thread(communicationService);

    public ProxyRobots(final IZmqMessageBroker mfProxy,
                       final IConfigServerGame configServerGame,
                       final IConfigRobots configRobots,
                       final IRobotsMap robotsMap) {
        logback.info("Constructor -- IN");
        assert (null != mfProxy);
        assert (null != configServerGame);
        assert (null != configRobots);
        assert (null != robotsMap);

        this.mfProxy = mfProxy;
        this.configServerGame = configServerGame;
        this.configRobots = configRobots;
        this.robotsMap = robotsMap;

        mfProxy.addZmqMessageListener(this);
        logback.info("Constructor -- OUT");
    }

    public static void main(final String[] args) throws Exception {
        final ConfigFactoryParameters configPathType = new Cli(args).parse();

        final ProxyRobots proxyRobots = new ProxyRobotsFactory(configPathType).getProxyRobots();
        if (null == proxyRobots) {
            logback.error("Error when creating ProxyRobots");
        } else {
            proxyRobots.start();
        }
    }

    private void connectToServer() {
        mfProxy.connectToServer(
                configServerGame.getIp(),
                configServerGame.getPushPort(),
                configServerGame.getSubPort());
    }

    /*
     * This instantiate Tanks objects from a configuration It only set up the
     * tanksInitializedMap
     */
    protected void initializeTanksFromConfig() {
        for (final ConfigTank configTank : configRobots.getConfigRobotsToRegister()) {
            final Camera camera = new Camera(configTank.getConfigCamera().getIp(),
                    configTank.getConfigCamera().getPort());
            //TODO Improve initialization of setImage to get something meaningful
            //from the string (like an actual picture)
            final Tank tank = new Tank(configTank.getBluetoothName(),
                    configTank.getBluetoothID(), camera, configTank.getImage());
            logback.info("Temporary routing ID: " + configTank.getTempRoutingID());
            tank.setRoutingId(configTank.getTempRoutingID());
            this.robotsMap.add(tank);
        }

        logback.info("All " + this.robotsMap.getRobotsArray().size()
                + " tank(s) initialized");
    }

    protected void connectToRobots() {
        for (final IRobot robot : robotsMap.getNotConnectedRobots()) {
            robot.connectToDevice();
        }
    }

    protected void sendRegister() {
        for (final IRobot robot : robotsMap.getConnectedRobots()) {
            robot.buildRegister();
            final ZmqMessageBOM zmqMessageBOM = new ZmqMessageBOM(robot.getRoutingId(), EnumMessageType.REGISTER,
                    robot.getRegisterBytes());
            mfProxy.sendZmqMessage(zmqMessageBOM);
            logback.info("Robot [" + robot.getRoutingId()
                    + "] is trying to register itself to the server!");
        }
    }

    protected void sendServerRobotStates() {
        for (final IRobot robot : robotsMap.getRegisteredRobots()) {
            final byte[] zmqServerRobotState = robot.getAndClearZmqServerRobotStateBytes();
            if (null != zmqServerRobotState) {
                logback.debug("Sending a ServerRobotState message");
                final ZmqMessageBOM zmqMessageBOM =
                        new ZmqMessageBOM(robot.getRoutingId(), EnumMessageType.SERVER_ROBOT_STATE,
                                robot.getRegisterBytes());
                mfProxy.sendZmqMessage(zmqMessageBOM);
            }
        }
    }

    protected void startCommunicationService() {
        communicationThread.start();
    }

    protected boolean isCommunicationServiceAlive() {
        return communicationThread.isAlive();
    }

    public void stop() {
        disconnectAllTanks();
    }

    private void disconnectAllTanks() {
        for (final IRobot tank : robotsMap.getConnectedRobots()) {
            tank.closeConnection();
        }
    }

    private void onRegistered(final ZmqMessageBOM zmqMessageBOM) {
        logback.info("Setting ServerGame Registered to tank");
        final String routingId = zmqMessageBOM.getRoutingId();
        if (robotsMap.isRobotConnected(routingId)) {
            final IRobot registeredRobot = robotsMap.get(routingId);
            registeredRobot.setRegistered(zmqMessageBOM.getMessageBodyBytes());
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a tank to register");
        }
    }

    private void onInput(final ZmqMessageBOM zmqMessageBOM) {
        logback.info("Setting controller Input to tank");
        final String routingId = zmqMessageBOM.getRoutingId();
        if (robotsMap.isRobotRegistered(routingId)) {
            final IRobot targetedRobot = robotsMap.get(routingId);
            targetedRobot.setControllerInput(zmqMessageBOM.getMessageBodyBytes());
            logback.info("tankTargeted input : " + targetedRobot.controllerInputToString());
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a registered tank");
        }
    }

    private void onGameState(final ZmqMessageBOM zmqMessageBOM) {
        logback.warn("Received GameState - not handled");
    }

    private void onDefault() {
        logback.warn("Unknown message type");
    }

    /*
     * Start the proxy :
     * -connect itself to the server
     * -initialize robots from a config
     * -connect to those robots
     * -start communication service with the server
     * -send register to the server
     */
    public void start() {
        this.connectToServer();
        this.initializeTanksFromConfig();
        this.connectToRobots();
        //We have to start the communication service before sending Register
        //Otherwise we risk not being ready to read Registered in time
        this.startCommunicationService();
        this.sendRegister();
    }

    @Override
    public void receivedNewZmq(final ZmqMessageBOM zmqMessageBOM) {
        switch (zmqMessageBOM.getMessageType()) {
            case REGISTERED:
                onRegistered(zmqMessageBOM);
                break;
            case INPUT:
                onInput(zmqMessageBOM);
                break;
            case GAME_STATE:
                onGameState(zmqMessageBOM);
                break;
            default:
                onDefault();
        }
    }

    private class CommunicationService implements Runnable {
        public void run() {
            logback.info("Start of communication service");

            while (!Thread.currentThread().isInterrupted() &&
                    !robotsMap.getConnectedRobots().isEmpty()) {
                sendServerRobotStates();
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException e) {
                    logback.error("CommunicationService thread sleep exception: " + e.getMessage());
                }
            }
            logback.info("End of communication service");
            mfProxy.close();
            Thread.yield();
        }
    }
}
