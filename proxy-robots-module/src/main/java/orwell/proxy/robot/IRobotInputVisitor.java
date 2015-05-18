package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public interface IRobotInputVisitor {
    void visit(final RobotMove robotMove);
    void visit(final RobotFire robotFire);
    void visit(final IRobot2 robot);
}
