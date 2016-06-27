package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 * Created by Michaël Ludmann on 26/06/16.
 */
public class LegoEv3Tank extends IRobot {
    private final static Logger logback = LoggerFactory.getLogger(LegoEv3Tank.class);
    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;

    public LegoEv3Tank(final String ipAddress, final String macAddress,
                       final int videoStreamPort, final String image,
                       int pushPort, int subPort) {
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
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket sender = context.socket(ZMQ.PUSH);
        ZMQ.Socket receiver = context.socket(ZMQ.PULL);

        sender.setLinger(1000);
        receiver.setLinger(1000);
        logback.debug("BIND");
        sender.bind("tcp://0.0.0.0:10000");
        receiver.bind("tcp://0.0.0.0:10001");
        logback.debug("BOUND");
        sender.send("PING".getBytes());
        byte[] msg = receiver.recv();
        logback.debug("Received message: " + new String(msg));
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
