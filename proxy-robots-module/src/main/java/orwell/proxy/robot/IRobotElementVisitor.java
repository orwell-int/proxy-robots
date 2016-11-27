package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public interface IRobotElementVisitor {
    void visit(final RfidSensor rfidSensor);

    void visit(final ColourSensor colourSensor);

    void visit(final ICamera camera);

    void visit(final IRobot robot);

    void visit(final UsSensor usSensor);

    void visit(final BatteryInfo batteryInfo);
}
