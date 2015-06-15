package orwell.proxy.mock;

import orwell.proxy.robot.ICamera;
import orwell.proxy.robot.IRobotElementVisitor;

/**
 * Created by Michaël Ludmann on 10/05/15.
 */
public class MockedCamera implements ICamera {

    @Override
    public String getUrl() {
        return "http://fake.url";
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MockedCamera { [URL] " + getUrl() + " }";
    }
}
