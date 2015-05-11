package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.*;
import orwell.proxy.robot.Camera;
import orwell.proxy.robot.IRobot;
import orwell.proxy.robot.IRobotsMap;
import orwell.proxy.robot.Tank;
import orwell.proxy.zmq.IZmqMessageBroker;
import orwell.proxy.zmq.IZmqMessageListener;
import orwell.proxy.zmq.ZmqMessageBOM;
import orwell.proxy.zmq.ZmqMessageDecoder;

public class ProxyRobots implements IZmqMessageListener {
    private final static Logger logback = LoggerFactory.getLogger(ProxyRobots.class);
    private final IConfigServerGame configServerGame;
    private final IConfigRobots configRobots;
    private final IZmqMessageBroker mfProxy;
    protected IRobotsMap robotsMap;
    private final CommunicationService communicationService = new CommunicationService();
    private final Thread communicationThread = new Thread(communicationService);
    private final long outgoingMessagePeriod;

    public ProxyRobots(final IZmqMessageBroker mfProxy,
                       final IConfigFactory configFactory,
                       final IRobotsMap robotsMap) {
        logback.info("Constructor -- IN");
        assert (null != mfProxy);
        assert (null != configFactory);
        assert (null != configFactory.getConfigProxy());
        assert (null != robotsMap);

        this.mfProxy = mfProxy;
        this.configServerGame = configFactory.getConfigServerGame();
        this.configRobots = configFactory.getConfigRobots();
        this.robotsMap = robotsMap;
        this.outgoingMessagePeriod = configFactory.getConfigProxy().getOutgoingMsgPeriod();

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

    /**
     * This instantiates Tanks objects from a configuration It only set up the
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
            final ZmqMessageBOM zmqMessageBOM = new ZmqMessageBOM(EnumMessageType.REGISTER, robot.getRoutingId(),
                    robot.getRegisterBytes());
            mfProxy.sendZmqMessage(zmqMessageBOM);
            logback.info("Robot [" + robot.getRoutingId()
                    + "] is trying to register itself to the server!");
        }
    }

    /**
     * Sends a delta of each robot state since last call
     */
    protected void sendServerRobotStates() {
        for (final IRobot robot : robotsMap.getRegisteredRobots()) {
            final byte[] zmqServerRobotState = robot.getServerRobotStateBytes_And_ClearDelta();
            if (null != zmqServerRobotState) {
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
            final IRobot registeredRobot = robotsMap.get(routingId);
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
            final IRobot targetedRobot = robotsMap.get(routingId);
            targetedRobot.setControllerInput(zmqMessage.getMessageBytes());
            logback.info("tankTargeted input : " + targetedRobot.controllerInputToString());
        } else {
            logback.info("RoutingID " + routingId
                    + " is not an ID of a registered tank");
        }
    }

    private void onGameState(final ZmqMessageDecoder zmqMessage) {
        logback.warn("Received GameState - not handled");
    }

    private void onDefault() {
        logback.warn("Unknown message type");
    }

    /**
     * Starts the proxy :
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
            case GAME_STATE:
                onGameState(msg);
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
                    !robotsMap.getConnectedRobots().isEmpty()) {

                // We avoid flooding the server
                if(outgoingMessagePeriod < System.currentTimeMillis() - lastSendTime) {
                    sendServerRobotStates();
                    lastSendTime = System.currentTimeMillis();
                }
                try {
                    // This is performed to avoid high CPU consumption
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
