package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michaël Ludmann on 26/06/16.
 */
public class LegoEv3Tank extends IRobot {
    private final static Logger logback = LoggerFactory.getLogger(LegoEv3Tank.class);
    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;

    public LegoEv3Tank(final String ipAdress, final String macAddress,
                       final int streamPort, final String image) {
        this.robotElements = new IRobotElement[]{new RfidSensor()};
        this.robotActions = new IRobotInput[]{new InputMove(), new InputFire()};
        setImage(image);
    }

    public void setRfidValue(final String rfidValue) {
        ((RfidSensor) robotElements[1]).setValue(rfidValue);
    }

    @Override
    public void sendUnitMessage(UnitMessage unitMessage) {

    }

    @Override
    public EnumConnectionState connect() {
        return null;
    }

    @Override
    public void closeConnection() {

    }

    @Override
    public void accept(IRobotElementVisitor visitor) {

    }

    @Override
    public void accept(IRobotInputVisitor visitor) {

    }
}
