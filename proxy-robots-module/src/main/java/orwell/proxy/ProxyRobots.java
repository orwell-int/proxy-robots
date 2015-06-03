package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.*;
import orwell.proxy.robot.*;
import orwell.proxy.zmq.IZmqMessageBroker;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ZmqMessageBOM;
import orwell.proxy.zmq.ZmqMessageBroker;

public class ProxyRobots implements IZmqMessageListener {
    private final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class);
    private static final long THREAD_SLEEP_MS = 10;
    private final IConfigServerGame configServerGame;
    private final IConfigRobots configRobots;
    private final IZmqMessageBroker messageBroker;
    private final CommunicationService communicationService = new CommunicationService();
    private final Thread communicationThread = new Thread(communicationService);
    private final long outgoingMessagePeriod;
    private final RobotFactory robotFactory;
    protected IRobotsMap robotsMap;
    protected int outgoingMessageFiltered;
    private UdpBeaconFinder udpBeaconFinder;

    public ProxyRobots(final IZmqMessageBroker messageBroker,
                       final IConfigFactory configFactory,
                       final IRobotsMap robotsMap) {
        logback.info("Constructor -- IN");
        assert (null != messageBroker);
        assert (null != configFactory);
        assert (null != configFactory.getConfigProxy());
        assert (null != robotsMap);

        this.messageBroker = messageBroker;
        this.configServerGame = configFactory.getMaxPriorityConfigServerGame();
        this.configRobots = configFactory.getConfigRobots();
        this.robotsMap = robotsMap;
        this.outgoingMessagePeriod = configFactory.getConfigProxy().getOutgoingMsgPeriod();

        robotFactory = new RobotFactory();
        messageBroker.addZmqMessageListener(this);
        logback.info("Constructor -- OUT");
    }

    public ProxyRobots(final UdpBeaconFinder udpBeaconFinder,
                       final ZmqMessageBroker zmqMessageFramework,
                       final ConfigFactory configFactory,
                       final RobotsMap robotsMap) {
        this(zmqMessageFramework, configFactory, robotsMap);
        this.udpBeaconFinder = udpBeaconFinder;
    }

    public static void main(final String[] args) throws Exception {
        final ConfigFactoryParameters configPathType = new Cli(args).parse();
        if (null == configPathType) {
            logback.warn("Command Line Interface did not manage to extract parameters. Exiting now.");
            System.exit(0);
        }

        final ProxyRobots proxyRobots = new ProxyRobotsFactory(configPathType).getProxyRobots();
        if (null == proxyRobots) {
            logback.error("Error when creating ProxyRobots");
        } else {
            proxyRobots.start();
        }
    }

    private void connectToServer() {
        if (null != udpBeaconFinder) {
            // We first try to find the server using Udp discovery
            udpBeaconFinder.broadcastAndGetServerAddress();
            if (udpBeaconFinder.hasFoundServer()) {
                messageBroker.connectToServer(udpBeaconFinder.getPushAddress(),
                        udpBeaconFinder.getSubscribeAddress());
            }
        } else {
            // If we fail, we use the configuration data
            messageBroker.connectToServer(configServerGame.getPushAddress(),
                    configServerGame.getSubscribeAddress());
        }
    }

    /**
     * This instantiates Robots objects from a configuration. It only sets up the
     * RobotsMap
     */
    protected void initializeRobotsFromConfig() {
        for (final IConfigRobot configRobot : configRobots.getConfigRobotsToRegister()) {
            final IRobot robot = robotFactory.getRobot(configRobot);
            if (null == robot) {
                logback.error("Robot not initialized. Skipping it for now.");
            } else {
                logback.info("Temporary routing ID: " + robot.getRoutingId());
                robot.setRoutingId(configRobot.getTempRoutingID());
                this.robotsMap.add(robot);
            }
        }

        logback.info("All " + this.robotsMap.getRobotsArray().size()
                + " robot(s) initialized");
    }

    protected void connectToRobots() {
        for (final IRobot robot : robotsMap.getNotConnectedRobots()) {
            robot.connect();
        }
    }

    protected void sendRegister() {
        for (final IRobot robot : robotsMap.getConnectedRobots()) {
            final ZmqMessageBOM zmqMessageBOM = new ZmqMessageBOM(robot.getRoutingId(), EnumMessageType.REGISTER,
                    RegisterBytes.fromRobotFactory(robot));
            messageBroker.sendZmqMessage(zmqMessageBOM);
            logback.info("Robot [" + robot.getRoutingId()
                    + "] is trying to register itself to the server!");
        }
    }

    /**
     * Sends a delta of each robot state since last call
     */
    protected void sendServerRobotStates() {
        for (final IRobot robot : robotsMap.getRegisteredRobots()) {
            final RobotElementStateVisitor stateVisitor = new RobotElementStateVisitor();
            robot.accept(stateVisitor);

            final byte[] serverRobotStateBytes = stateVisitor.getServerRobotStateBytes();
            if (null != serverRobotStateBytes) {
                logback.debug("Sending a ServerRobotState message");
                final ZmqMessageBOM zmqMessageBOM =
                        new ZmqMessageBOM(robot.getRoutingId(), EnumMessageType.SERVER_ROBOT_STATE,
                                serverRobotStateBytes);
                messageBroker.sendZmqMessage(zmqMessageBOM);
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
        disconnectAllRobots();
    }

    private void disconnectAllRobots() {
        for (final IRobot robot : robotsMap.getConnectedRobots()) {
            robot.closeConnection();
        }
    }

    private void onRegistered(final ZmqMessageBOM zmqMessageBOM) {
        logback.info("Setting ServerGame Registered to robot");
        final String routingId = zmqMessageBOM.getRoutingId();
        if (robotsMap.isRobotConnected(routingId)) {
            final IRobot registeredRobot = robotsMap.get(routingId);
            final Registered registered = new Registered(zmqMessageBOM.getMessageBodyBytes());
            registered.setToRobot(registeredRobot);
            final RobotElementPrintVisitor printVisitor = new RobotElementPrintVisitor();
            registeredRobot.accept(printVisitor);
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a robot to register");
        }
    }

    private void onInput(final ZmqMessageBOM zmqMessageBOM) {
        logback.info("Setting controller Input to robot");
        final String routingId = zmqMessageBOM.getRoutingId();
        if (robotsMap.isRobotRegistered(routingId)) {
            final IRobot targetedRobot = robotsMap.get(routingId);
            final RobotInputSetVisitor inputSetVisitor = new RobotInputSetVisitor(zmqMessageBOM.getMessageBodyBytes());
            targetedRobot.accept(inputSetVisitor);
            logback.info("robotTargeted input : " + inputSetVisitor.inputToString(targetedRobot));
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a registered robot");
        }
    }

    private void onGameState(final ZmqMessageBOM zmqMessageBOM) {
        logback.warn("Received GameState - not handled (robot " + zmqMessageBOM.getRoutingId() + ")");
    }

    private void onDefault() {
        logback.warn("Unknown message type");
    }

    /**
     * Starts the proxy :
     * -connect itself to the server
     * -initialize robots from a config if the provided map is empty
     * -connect to those robots
     * -start communication service with the server
     * -send register to the server
     */
    public void start() {
        this.connectToServer();
        if (robotsMap.getRobotsArray().isEmpty())
            this.initializeRobotsFromConfig();
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
            long lastSendTime = System.currentTimeMillis();

            // We stop the service once there are no more robots
            // connected to the proxy
            while (!Thread.currentThread().isInterrupted() &&
                    !robotsMap.getConnectedRobots().isEmpty() &&
                    messageBroker.isConnectedToServer()) {

                // We avoid flooding the server
                if (outgoingMessagePeriod < System.currentTimeMillis() - lastSendTime) {
                    sendServerRobotStates();
                    lastSendTime = System.currentTimeMillis();
                } else {
                    outgoingMessageFiltered++;
                }
                try {
                    // This is performed to avoid high CPU consumption
                    Thread.sleep(THREAD_SLEEP_MS);
                } catch (final InterruptedException e) {
                    logback.error("CommunicationService thread sleep exception: " + e.getMessage());
                }
            }
            logback.info("End of communication service");
            messageBroker.close();
            Thread.yield();
        }
    }
}
