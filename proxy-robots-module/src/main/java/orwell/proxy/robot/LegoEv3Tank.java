package orwell.proxy.robot;

import lejos.mf.common.IUnitMessage;
import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.SimpleUnitMessage;
import lejos.mf.common.StreamUnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.zmq.RobotMessageBroker;

/**
 * Created by Michaël Ludmann on 26/06/16.
 */
public class LegoEv3Tank extends IRobot implements MessageListenerInterface {
    private final static Logger logback = LoggerFactory.getLogger(LegoEv3Tank.class);
    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;
    private final RobotMessageBroker robotMessageBroker;
    private final String hostname;
    private final String ipAddress;

    public LegoEv3Tank(final String ipAddress, final String macAddress,
                       final int videoStreamPort, final String image,
                       int pushPort, int pullPort, String hostname) {
        this.robotElements = new IRobotElement[]{new RfidSensor()};
        this.robotActions = new IRobotInput[]{new InputMove(), new InputFire()};
        setImage(image);
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        robotMessageBroker = new RobotMessageBroker(pushPort, pullPort);
        setCameraUrl("nc:" + ipAddress + ":" + videoStreamPort);
    }

    @Override
    public void setRfidValue(final String rfidValue) {
        ((RfidSensor) robotElements[1]).setValue(rfidValue);
    }

    @Override
    public void setColourValue(final String colourValue) {
        ((ColourSensor) robotElements[2]).setValue(colourValue);
    }


    @Override
    public void sendUnitMessage(final IUnitMessage unitMessage) throws MessageNotSentException {
        if (!sendMessageSucceeds(unitMessage)) {
            logback.error("Unable to send message to robot " + this.hostname + ". ABORT!");
            throw new MessageNotSentException(hostname);
        }
    }

    private boolean sendMessageSucceeds(IUnitMessage unitMessage) {
        return robotMessageBroker.send((SimpleUnitMessage) unitMessage);
    }

    @Override
    public EnumConnectionState connect() {
        robotMessageBroker.bind();
        setConnectionState(EnumConnectionState.CONNECTED);
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

    }

    @Override
    public void accept(IRobotInputVisitor visitor) {

    }

    @Override
    public void receivedNewMessage(StreamUnitMessage msg) {

    }

    @Override
    public String toString() {
        return "LegoEv3Tank { [Hostname] " + hostname + " [IP] " +
                ipAddress + " [RoutingID] " + getRoutingId() +
                " [TeamName] " + getTeamName() + " }";
    }
}
