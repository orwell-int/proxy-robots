package orwell.proxy.mock;

import orwell.proxy.robot.ICamera;
import orwell.proxy.robot.IRobotElementVisitor;

/**
 * Created by Michaël Ludmann on 10/05/15.
 */
public class MockedCamera implements ICamera {
    public MockedCamera() {
    }

    @Override
    public String getUrl() {
        return "http://fake.url";
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }
}
