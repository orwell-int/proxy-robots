package orwell.proxy.mock;

import orwell.proxy.config.IConfigCamera;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
public class MockedConfigCamera implements IConfigCamera {

    private String resourcePath = "/mockedResourcePath";
    private final static int PORT = 777;

    @Override
    public String getIp() {
        return "mockedIp";
    }

    @Override
    public int getPort() {
        return PORT;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(final String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
