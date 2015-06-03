package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;


/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RobotElementStateVisitor implements IRobotElementVisitor {
    private final static Logger logback = LoggerFactory.getLogger(RobotElementStateVisitor.class);
    private final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();

    protected Robot.ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    /**
     * @return the byte array of ServerRobotState or
     * null if both rfid and colour list are empty
     */
    public byte[] getServerRobotStateBytes() {
        final Robot.ServerRobotState serverRobotState = getServerRobotState();
        if (null == serverRobotState ||
                (serverRobotState.getRfidList().isEmpty() &&
                        serverRobotState.getColourList().isEmpty()))
            return null;
        else
            return serverRobotState.toByteArray();
    }

    public void clearServerRobotState() {
        serverRobotStateBuilder.clear();
    }

    @Override
    public void visit(final RfidSensor rfidSensor) {

        serverRobotStateBuilder.addAllRfid(rfidSensor.getRfidSensorReads());
        rfidSensor.clear();
    }

    @Override
    public void visit(final ColourSensor colourSensor) {

        serverRobotStateBuilder.addAllColour(colourSensor.getColourSensorReads());
        colourSensor.clear();
    }

    @Override
    public void visit(final ICamera camera) {
<<<<<<< HEAD
        ;
=======

>>>>>>> master
    }

    @Override
    public void visit(final IRobot robot) {
<<<<<<< HEAD
        ;
=======

>>>>>>> master
    }
}
