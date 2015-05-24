package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public interface IRobotElement {
    void accept(final IRobotElementVisitor visitor);
}
