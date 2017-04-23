package orwell.proxy.robot;

public interface IRobotElementVisitor {
    void visit(final ColourSensor colourSensor);

    void visit(final ICamera camera);

    void visit(final IRobot robot);

    void visit(final UsSensor usSensor);

    void visit(final BatteryInfo batteryInfo);
}
