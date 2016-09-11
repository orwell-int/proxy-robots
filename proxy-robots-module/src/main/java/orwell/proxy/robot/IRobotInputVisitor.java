package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public interface IRobotInputVisitor {
    void visit(final InputMove inputMove);

    void visit(final InputFire inputFire);

    void visit(final IRobot robot) throws MessageNotSentException;
}
