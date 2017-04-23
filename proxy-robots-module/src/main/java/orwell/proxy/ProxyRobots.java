package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.Configuration;
import orwell.proxy.config.IConfigFactory;
import orwell.proxy.config.elements.*;
import orwell.proxy.robot.*;
import orwell.proxy.udp.RobotsDiscoveryThread;
import orwell.proxy.udp.UdpServerGameFinder;
import orwell.proxy.zmq.IServerGameMessageBroker;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ZmqMessageBOM;

public class ProxyRobots implements IZmqMessageListener {
    private final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class);
    private static final long THREAD_SLEEP_MS = 10;
    private final IConfigServerGame configServerGame;
    private final IConfigRobots configRobots;
    private final IServerGameMessageBroker messageBroker;
    private final CommunicationService communicationService = new CommunicationService();
    private final Thread communicationThread = new Thread(communicationService);
    private final RobotsPortsPool robotsPortsPool;
    protected IRobotsMap robotsMap;
    private int outgoingMessageFiltered;
    private UdpServerGameFinder udpServerGameFinder;
    private int udpProxyBroadcastPort;


    public ProxyRobots(
            final IServerGameMessageBroker messageBroker,
            final IConfigFactory configFactory,
            final IRobotsMap robotsMap,
            final ConfigRobotsPortsPool configRobotsPortsPool,
            final int udpProxyBroadcastPort) {
        assert null != messageBroker;
        assert null != configFactory;
        assert null != robotsMap;

        this.messageBroker = messageBroker;
        this.configServerGame = configFactory.getMaxPriorityConfigServerGame();
        this.configRobots = configFactory.getConfigRobots();
        this.robotsMap = robotsMap;

        messageBroker.addZmqMessageListener(this);

        robotsPortsPool = new RobotsPortsPool(
                configRobotsPortsPool.getBeginPort(),
                configRobotsPortsPool.getPortsCount());

        this.udpProxyBroadcastPort = udpProxyBroadcastPort;
    }

    public ProxyRobots(final UdpServerGameFinder udpServerGameFinder,
                       final IServerGameMessageBroker serverGameMessageBroker,
                       final IConfigFactory configFactory,
                       final IRobotsMap robotsMap,
                       final ConfigRobotsPortsPool configRobotsPortsPool,
                       final int udpProxyBroadcastPort) {
        this(serverGameMessageBroker, configFactory, robotsMap, configRobotsPortsPool, udpProxyBroadcastPort);
        this.udpServerGameFinder = udpServerGameFinder;
    }

    public static void main(final String[] args) throws Exception {
        final Configuration configuration = new Cli(args).parse();
        if (null == configuration) {
            logback.warn("Command Line Interface did not manage to extract a configuration. Exiting now.");
            System.exit(0);
        }

        final ProxyRobots proxyRobots = new ProxyRobotsFactory(configuration).getProxyRobots();
        if (null == proxyRobots) {
            logback.error("Error when creating ProxyRobots. Exiting now.");
            System.exit(0);
        } else {
            proxyRobots.start();
        }
    }

    private void connectToServer() {
        if (null != udpServerGameFinder) {
            // We first try to find the server using Udp discovery
            udpServerGameFinder.broadcastAndGetServerAddress();
            if (udpServerGameFinder.hasFoundServer()) {
                messageBroker.connectToServer(udpServerGameFinder.getPushAddress(),
                        udpServerGameFinder.getSubscribeAddress());
                return;
            }
        }
        // If there is no beaconFinder or if it fails, we use the configuration data
        messageBroker.connectToServer(configServerGame.getPushAddress(),
                configServerGame.getSubscribeAddress());
    }

    /**
     * This instantiates Robots objects from a configuration. It only sets up the
     * RobotsMap
     */
    protected void initializeRobotsFromConfig() throws ConfigRobotException {
        for (final IConfigRobot configRobot : configRobots.getConfigRobotsToRegister()) {
            final IRobot robot = RobotFactory.getRobot(configRobot);
            if (null == robot) {
                logback.error("One robot from config not initialized. Skipping it for now.");
            } else {
                robot.setRoutingId(configRobot.getTempRoutingID());
                logback.info("Initializing one robot: its temporary routing ID is [" + robot.getRoutingId() + "]");
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
        waitForConnectionAck();
    }

    private void waitForConnectionAck() {
        while (!robotsMap.getNotConnectedRobots().isEmpty()) {
            try {
                Thread.sleep(THREAD_SLEEP_MS);
            } catch (InterruptedException e) {
                logback.error(e.getMessage());
            }
        }
        logback.info("All " + robotsMap.getConnectedRobots().size() + " robot(s) are connected");
    }

    protected void sendRegister() {
        for (final IRobot robot : robotsMap.getConnectedRobots()) {
            final ZmqMessageBOM zmqMessageBOM = new ZmqMessageBOM(robot.getRoutingId(), EnumMessageType.REGISTER,
                    RegisterBytes.fromRobotFactory(robot));
            boolean isSendSuccessful = messageBroker.sendZmqMessage(zmqMessageBOM);
            if (!isSendSuccessful) {
                logback.error("Failed to send a Register message for " + robot.getRoutingId());
            }
            logback.info("Robot [" + robot.getRoutingId()
                    + "] is trying to register itself to the server!");
        }
    }

    /**
     * Sends a delta of each robot state since last call to this method
     */
    protected void sendServerRobotStates() {
        for (final IRobot robot : robotsMap.getRegisteredRobots()) {
            final RobotElementStateVisitor stateVisitor = new RobotElementStateVisitor();
            robot.accept(stateVisitor);

            final byte[] serverRobotStateBytes = stateVisitor.getServerRobotStateBytes();
            if (null != serverRobotStateBytes) {
                final ZmqMessageBOM zmqMessageBOM =
                        new ZmqMessageBOM(robot.getRoutingId(), EnumMessageType.SERVER_ROBOT_STATE,
                                serverRobotStateBytes);
                messageBroker.sendZmqMessage(zmqMessageBOM);
                logback.debug("Sending a ServerRobotState message: " + stateVisitor.toString());
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
        messageBroker.close();
    }

    private void disconnectAllRobots() {
        for (final IRobot robot : robotsMap.getConnectedRobots()) {
            robot.closeConnection();
        }
    }

    private void onRegistered(final ZmqMessageBOM zmqMessageBOM) {
        final String routingId = zmqMessageBOM.getRoutingId();
        logback.info("Setting ServerGame Registered to robot " + routingId);
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
        final String routingId = zmqMessageBOM.getRoutingId();
        if (robotsMap.isRobotRegistered(routingId)) {
            applyInputOnRobot(zmqMessageBOM, routingId);
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a registered robot");
        }
    }

    private void applyInputOnRobot(ZmqMessageBOM input, String routingId) {
        final IRobot targetedRobot = robotsMap.get(routingId);
        final RobotInputSetVisitor inputSetVisitor = new RobotInputSetVisitor(input.getMessageBodyBytes());
        logback.debug(inputSetVisitor.toString(targetedRobot));
        try {
            targetedRobot.accept(inputSetVisitor);
        } catch (MessageNotSentException e) {
            logback.error(e.getMessage());
            this.stop();
        }
    }

    private void onGameState(final ZmqMessageBOM zmqMessageBOM) {
        //logback.debug("Setting new GameState");
        final GameState gameState = new GameState(zmqMessageBOM.getMessageBodyBytes());
        try {
            robotsMap.accept(gameState.getRobotGameStateVisitor());
        } catch (Exception e) {
            logback.error(e.getMessage());
            this.stop();
        }
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
    public void start() throws ConfigRobotException {
        this.connectToServer();
        if (robotsMap.getRobotsArray().isEmpty())
            this.initializeRobotsFromConfig();
        this.broadcastServerIp();
        this.connectToRobots();
        //We have to start the communication service before sending Register
        //Otherwise we risk not being ready to read Registered in time
        this.startCommunicationService();
        this.sendRegister();
    }

    private void broadcastServerIp() {
        Thread robotsDiscoveryThread = new Thread(new RobotsDiscoveryThread(
                robotsPortsPool.getAvailablePort(),
                robotsPortsPool.getAvailablePort(),
                udpProxyBroadcastPort));
        robotsDiscoveryThread.start();
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

    protected int getOutgoingMessageFiltered() {
        return outgoingMessageFiltered;
    }

    private class CommunicationService implements Runnable {
        public void run() {
            logback.info("Start of communication service");
            long lastSendTime = 0;

            // We stop the service once there are no more robots
            // connected to the proxy
            while (shouldRunCommunicationService()) {

                // We avoid flooding the server
                if (messageBroker.getOutgoingMessagePeriod() < System.currentTimeMillis() - lastSendTime) {
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
            terminateCommunicationService();
        }

        private void terminateCommunicationService() {
            logback.info("End of communication service");
            messageBroker.close();
            Thread.yield();
        }

        private boolean shouldRunCommunicationService() {
            return !Thread.currentThread().isInterrupted() &&
                    !robotsMap.getConnectedRobots().isEmpty() &&
                    messageBroker.isConnectedToServer();
        }
    }
}
