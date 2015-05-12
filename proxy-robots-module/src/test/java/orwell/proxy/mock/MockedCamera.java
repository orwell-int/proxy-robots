package orwell.proxy.mock;

import orwell.proxy.robot.ICamera;

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
}
