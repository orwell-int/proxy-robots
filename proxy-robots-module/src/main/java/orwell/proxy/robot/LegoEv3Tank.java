package orwell.proxy.robot;

import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.UnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.zmq.RobotMessageBroker;

public class LegoEv3Tank extends IRobot implements MessageListenerInterface {
    private final static Logger logback = LoggerFactory.getLogger(LegoEv3Tank.class);
    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;
    private final RobotMessageBroker robotMessageBroker;
    private final String hostname;
    private final String ipAddress;
    private final UnitMessageBroker unitMessageBroker = new UnitMessageBroker(this);

    public LegoEv3Tank(final String ipAddress,
                       final int videoStreamPort, final String image,
                       int pushPort, int pullPort, String hostname) {
        this(ipAddress, videoStreamPort, image, hostname,
                new RobotMessageBroker(pushPort, pullPort));
    }

    public LegoEv3Tank(final String ipAddress,
                       final int videoStreamPort, final String image,
                       String hostname, final RobotMessageBroker messageBroker) {
        this.robotElements = new IRobotElement[]{new UsSensor(), new ColourSensor(), new BatteryInfo()};
        this.robotActions = new IRobotInput[]{new InputMove(), new InputFire()};
        setImage(image);
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.robotMessageBroker = messageBroker;
        robotMessageBroker.addMessageListener(this);
        setCameraUrl("nc:" + ipAddress + ":" + videoStreamPort);
    }

    @Override
    public void setUsValue(int usValue) {
        ((UsSensor) robotElements[0]).setValue(usValue);
    }

    @Override
    public void setColourValue(final String colourValue) {
        ((ColourSensor) robotElements[1]).setValue(colourValue);
    }

    @Override
    public void setBatteryValues(final String batteryValue) {
        ((BatteryInfo) robotElements[2]).setValue(batteryValue);
    }

    @Override
    public void sendUnitMessage(final UnitMessage unitMessage) throws MessageNotSentException {
        if (!sendMessageSucceeds(unitMessage)) {
            logback.error("Unable to send message to robot " + this.hostname + ". ABORT!");
            throw new MessageNotSentException(hostname);
        }
    }

    private boolean sendMessageSucceeds(UnitMessage unitMessage) {
        boolean isSendSuccessful = robotMessageBroker.send(unitMessage);
        if (!isSendSuccessful) {
            logback.error("Failed to send UnitMessage to robot " + hostname);
        }
        return true;
    }

    @Override
    public EnumConnectionState connect() {
        robotMessageBroker.bind();
        return getConnectionState();
    }

    @Override
    public void closeConnection() {
        if (EnumConnectionState.CONNECTED == getConnectionState()) {
            robotMessageBroker.close();
        }
        setConnectionState(EnumConnectionState.NOT_CONNECTED);
    }

    @Override
    public void accept(IRobotElementVisitor visitor) {
        for (final IRobotElement element : robotElements) {
            element.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void accept(IRobotInputVisitor visitor) throws MessageNotSentException {
        for (final IRobotInput action : robotActions) {
            action.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void receivedNewMessage(UnitMessage message) {
        unitMessageBroker.handle(message);
    }

    @Override
    public String toString() {
        return "LegoEv3Tank { [Hostname] " + hostname + " [IP] " +
                ipAddress + " [RoutingID] " + getRoutingId() +
                " [TeamName] " + getTeamName() + " }";
    }
}
