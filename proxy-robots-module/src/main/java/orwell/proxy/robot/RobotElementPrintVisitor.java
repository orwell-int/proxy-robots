package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RobotElementPrintVisitor implements IRobotElementVisitor {
    private final static Logger logback = LoggerFactory.getLogger(RobotElementPrintVisitor.class);

    @Override
    public void visit(final RfidSensor rfidSensor) {
        logback.info(rfidSensor.toString());
    }

    @Override
    public void visit(final ColourSensor colourSensor) {
        logback.info(colourSensor.toString());
    }

    @Override
    public void visit(UsSensor usSensor) {
        logback.info(usSensor.toString());
    }

    @Override
    public void visit(BatteryInfo batteryInfo) {
        logback.info(batteryInfo.toString());
    }

    @Override
    public void visit(final ICamera camera) {
        logback.info(camera.toString());
    }

    @Override
    public void visit(final IRobot robot) {
        logback.info(robot.toString());
    }
}
