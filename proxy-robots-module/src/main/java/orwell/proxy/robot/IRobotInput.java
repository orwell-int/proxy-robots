package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public interface IRobotInput {
    void accept(final IRobotInputVisitor visitor) throws MessageNotSentException;
}
