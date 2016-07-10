package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.StreamUnitMessage;
import lejos.mf.pc.MessageFramework;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LegoNxtTank extends IRobot implements MessageListenerInterface {
    private final static Logger logback = LoggerFactory.getLogger(LegoNxtTank.class);
    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;
    private final NXTInfo nxtInfo;
    private final MessageFramework messageFramework;
    private final UnitMessageBroker unitMessageBroker = new UnitMessageBroker(this);


    public LegoNxtTank(final String bluetoothName, final String bluetoothId,
                       final MessageFramework messageFramework,
                       final ICamera camera, final String image) {
        this.robotElements = new IRobotElement[]{camera, new RfidSensor(), new ColourSensor()};
        this.robotActions = new IRobotInput[]{new InputMove(), new InputFire()};
        this.nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, bluetoothName, bluetoothId);
        this.messageFramework = messageFramework;
        messageFramework.addMessageListener(this);
        setImage(image);
        setCameraUrl(camera.getUrl());
    }

    public LegoNxtTank(final String bluetoothName, final String bluetoothId,
                       final ICamera camera, final String image) {
        this(bluetoothName, bluetoothId, new MessageFramework(), camera, image);
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
    public void receivedNewMessage(final UnitMessage msg) {
        unitMessageBroker.handle((StreamUnitMessage) msg);
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        for (final IRobotElement element : robotElements) {
            element.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void accept(final IRobotInputVisitor visitor) throws MessageNotSentException {
        for (final IRobotInput action : robotActions) {
            action.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void sendUnitMessage(final UnitMessage unitMessage) throws MessageNotSentException {
        logback.debug("Sending input to physical device");
        messageFramework.SendMessage((StreamUnitMessage) unitMessage);
    }

    @Override
    public EnumConnectionState connect() {
        logback.info("Connecting to robot: " + this.getRoutingId());

        final Boolean isConnected = messageFramework.ConnectToNXT(nxtInfo);
        if (isConnected) {
            setConnectionState(EnumConnectionState.CONNECTED);
            logback.info("Robot [" + getRoutingId() + "] is connected to the proxy!");
        } else {
            setConnectionState(EnumConnectionState.CONNECTION_FAILED);
            logback.warn("Robot [" + getRoutingId() + "] failed to connect to the proxy!");
        }
        return getConnectionState();
    }

    @Override
    public void closeConnection() {
        if (EnumConnectionState.CONNECTED == getConnectionState()) {
            messageFramework.close();
        }
        setConnectionState(EnumConnectionState.NOT_CONNECTED);
    }

    @Override
    public String toString() {
        return "LegoNxtTank { [BTName] " + nxtInfo.name + " [BT-ID] " +
                nxtInfo.deviceAddress + " [RoutingID] " + getRoutingId() +
                " [TeamName] " + getTeamName() + " }";
    }
}
