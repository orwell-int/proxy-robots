package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCli;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.IConfigRobots;
import orwell.proxy.config.IConfigServerGame;

public class ProxyRobots implements IZmqMessageListener {
    final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class);
    protected IMessageFramework mfProxy;
    protected IRobotsMap robotsMap;
    protected CommunicationService communicationService = new CommunicationService();
    private IConfigServerGame configServerGame;
    private IConfigRobots configRobots;
    private Thread communicationThread = new Thread(communicationService);

    public ProxyRobots(IMessageFramework mfProxy,
                       IConfigServerGame configServerGame,
                       IConfigRobots configRobots,
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
        ConfigCli configPathType = new Cli(args).parse();

        ProxyRobots proxyRobots = new ProxyRobotsFactory(configPathType, "platypus").getProxyRobots();
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

    protected void connectToRobots() {
        for (IRobot robot : robotsMap.getNotConnectedRobots()) {
            robot.connectToDevice();
        }
    }

    protected void sendRegister() {
        for (IRobot robot : robotsMap.getConnectedRobots()) {
            robot.buildRegister();
            mfProxy.sendZmqMessage(EnumMessageType.REGISTER, robot.getRoutingID(),
                    robot.getRegisterBytes());
            logback.info("Robot [" + robot.getRoutingID()
                    + "] is trying to register itself to the server!");
        }
    }

    protected void sendServerRobotStates() {
        for (IRobot robot : robotsMap.getRegisteredRobots()) {
            byte[] zmqServerRobotState = robot.getAndClearZmqServerRobotStateBytes();
            if (null == zmqServerRobotState) {
                continue;
            } else {
                logback.debug("Sending a ServerRobotState message");
                mfProxy.sendZmqMessage(EnumMessageType.SERVER_ROBOT_STATE,
                        robot.getRoutingID(), zmqServerRobotState);
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
