package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.IConfigRobots;
import orwell.proxy.config.IConfigServerGame;

public class ProxyRobots implements IZmqMessageListener {
    final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class);
    private final IConfigServerGame configServerGame;
    private final IConfigRobots configRobots;
    protected IZmqMessageFramework mfProxy;
    protected IRobotsMap robotsMap;
    protected CommunicationService communicationService = new CommunicationService();
    private final Thread communicationThread = new Thread(communicationService);

    public ProxyRobots(final IZmqMessageFramework mfProxy,
                       final IConfigServerGame configServerGame,
                       final IConfigRobots configRobots,
                       IRobotsMap robotsMap) {
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

    public static void main(String[] args) throws Exception {
        final ConfigCli configPathType = new Cli(args).parse();

        final ProxyRobots proxyRobots = new ProxyRobotsFactory(configPathType).getProxyRobots();
        proxyRobots.start();
    }

    protected void connectToServer() {
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
            Tank tank = new Tank(configTank.getBluetoothName(),
                    configTank.getBluetoothID(), camera, configTank.getImage());
            logback.info("Temporary routing ID: " + configTank.getTempRoutingID());
            tank.setRoutingId(configTank.getTempRoutingID());
            this.robotsMap.add(tank);
        }

        logback.info("All " + this.robotsMap.getRobotsArray().size()
                + " tank(s) initialized");
    }

    protected void connectToRobots() {
        for (IRobot robot : robotsMap.getNotConnectedRobots()) {
            robot.connectToDevice();
        }
    }

    protected void sendRegister() {
        for (IRobot robot : robotsMap.getConnectedRobots()) {
            robot.buildRegister();
            final ZmqMessageBOM zmqMessageBOM = new ZmqMessageBOM(EnumMessageType.REGISTER, robot.getRoutingId(),
                    robot.getRegisterBytes());
            mfProxy.sendZmqMessage(zmqMessageBOM);
            logback.info("Robot [" + robot.getRoutingId()
                    + "] is trying to register itself to the server!");
        }
    }

    protected void sendServerRobotStates() {
        for (final IRobot robot : robotsMap.getRegisteredRobots()) {
            final byte[] zmqServerRobotState = robot.getAndClearZmqServerRobotStateBytes();
            if (null == zmqServerRobotState) {
                continue;
            } else {
                logback.debug("Sending a ServerRobotState message");
                final ZmqMessageBOM zmqMessageBOM =
                        new ZmqMessageBOM(EnumMessageType.SERVER_ROBOT_STATE,
                                robot.getRoutingId(),
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

    private void onRegistered(final ZmqMessageDecoder zmqMessage) {
        logback.info("Setting ServerGame Registered to tank");
        final String routingId = zmqMessage.getRoutingId();
        if (robotsMap.isRobotConnected(routingId)) {
            IRobot registeredRobot = robotsMap.get(routingId);
            registeredRobot.setRegistered(zmqMessage.getMessageBytes());
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a tank to register");
        }
    }

    private void onInput(final ZmqMessageDecoder zmqMessage) {
        logback.info("Setting controller Input to tank");
        final String routingId = zmqMessage.getRoutingId();
        if (robotsMap.isRobotRegistered(routingId)) {
            IRobot robotTargeted = robotsMap.get(routingId);
            robotTargeted.setControllerInput(zmqMessage.getMessageBytes());
            logback.info("tankTargeted input : " + robotTargeted.controllerInputToString());
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a registered tank");
        }
    }

    private void onGameState(ZmqMessageDecoder zmqMessage) {
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
    public void receivedNewZmq(final ZmqMessageDecoder msg) {
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
        public void run() {
            logback.info("Start of communication service");

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
