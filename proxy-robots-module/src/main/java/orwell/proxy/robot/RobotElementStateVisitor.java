package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orwell.messages.Robot;
import orwell.messages.Robot.Rfid;


/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RobotElementStateVisitor implements IRobotElementVisitor{
    private final static Logger logback = LoggerFactory.getLogger(RobotElementStateVisitor.class);
    private final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();



    @Override
    public void visit(final RfidSensor rfid) {
        logback.debug("State rfid");


        serverRobotStateBuilder.addAllRfid(rfid.getRfidSensorReads());
        while(!rfid.getRfidSensorReads().isEmpty()) {
            serverRobotStateBuilder.addRfid(rfid.getRfidSensorReads().pollLast());
        }

    }

    @Override
    public void visit(final ColourSensor colourSensor) {
        logback.debug("State colour");
    }

    @Override
    public void visit(final ICamera camera) {
        logback.debug("State camera");
    }

    @Override
    public void visit(final IRobot robot) {
        logback.debug("State robot");
    }
}
