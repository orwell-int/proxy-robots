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


    public Robot.ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    @Override
    public void visit(final RfidSensor rfidSensor) {

        logback.debug("State rfid");
        serverRobotStateBuilder.addAllRfid(rfidSensor.getRfidSensorReads());
        rfidSensor.clear();
    }

    @Override
    public void visit(final ColourSensor colourSensor) {

        logback.debug("State colour");
        serverRobotStateBuilder.addAllColour(colourSensor.getColourSensorReads());
        colourSensor.clear();
    }

    @Override
    public void visit(final ICamera camera) {
        logback.debug("State camera");
    }

    @Override
    public void visit(final IRobot2 robot) {
        logback.debug("State robot");
    }
}
