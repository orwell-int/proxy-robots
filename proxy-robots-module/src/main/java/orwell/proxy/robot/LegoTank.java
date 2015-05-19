package orwell.proxy.robot;

import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.UnitMessage;
import lejos.mf.pc.MessageFramework;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnectionState;
import lejos.pc.comm.NXTInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LegoTank extends IRobot2 implements MessageListenerInterface {
    private final static Logger logback = LoggerFactory.getLogger(LegoTank.class);
    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;
    private final NXTInfo nxtInfo;
    private final MessageFramework messageFramework;
    private final UnitMessageBroker unitMessageBroker = new UnitMessageBroker(this);


    public LegoTank(final String bluetoothName, final String bluetoothId,
                    final MessageFramework messageFramework,
                    final ICamera camera, final String image) {
        this.robotElements = new IRobotElement[]{camera, new RfidSensor(), new ColourSensor()};
        this.robotActions = new IRobotInput[]{new RobotMove(), new RobotFire()};
        this.nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, bluetoothName, bluetoothId);
        this.messageFramework = messageFramework;
        messageFramework.addMessageListener(this);
        this.image = image;
        this.cameraUrl = camera.getUrl();
    }

    public LegoTank(final String bluetoothName, final String bluetoothId,
                    final ICamera camera, final String image) {
        this(bluetoothName, bluetoothId, new MessageFramework(), camera, image);
    }


    public void setRfidValue(final String rfidValue) {
        ((RfidSensor) robotElements[1]).setValue(rfidValue);
    }

    public void setColourValue(final String colourValue) {
        ((ColourSensor) robotElements[2]).setValue(colourValue);
    }


    @Override
    public void receivedNewMessage(final UnitMessage msg) {
        unitMessageBroker.handle(msg);
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        for (final IRobotElement element : robotElements) {
            element.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void accept(final IRobotInputVisitor visitor) {
        for (final IRobotInput action : robotActions) {
            action.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void sendUnitMessage(final UnitMessage unitMessage) {

        logback.debug("Sending input to physical device");
        messageFramework.SendMessage(unitMessage);
    }

    @Override
    EnumConnectionState connect() {
        logback.info("Connecting to robot: \n" + toString());

        final Boolean isConnected = messageFramework.ConnectToNXT(nxtInfo);
        if (isConnected) {
            this.connectionState = EnumConnectionState.CONNECTED;
            logback.info("Robot [" + routingId + "] is isConnected to the proxy!");
        } else {
            this.connectionState = EnumConnectionState.CONNECTION_FAILED;
            logback.warn("Robot [" + routingId + "] failed to connect to the proxy!");
        }
        return this.connectionState;
    }

    @Override
    void closeConnection() {
        if (EnumConnectionState.CONNECTED == connectionState
                && NXTConnectionState.DISCONNECTED != nxtInfo.connectionState) {
            messageFramework.close();
        }
    }

    @Override
    public String toString() {
        return "Tank {[BTName] " + nxtInfo.name + " [BT-ID] "
                + nxtInfo.deviceAddress + " [RoutingID] " + routingId + "}";
    }
}
