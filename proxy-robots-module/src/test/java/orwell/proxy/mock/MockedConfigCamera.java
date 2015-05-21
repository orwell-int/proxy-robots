package orwell.proxy.mock;

import orwell.proxy.config.IConfigCamera;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
public class MockedConfigCamera implements IConfigCamera {

    private static String resourcePath = "/mockedResourcePath";
    private static int port = 777;
    private static final String ip = "mockedIp";

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public static String getDefaultUrl() {
        return "http://"+ ip + ":" + Integer.toString(port) + resourcePath;
    }
}
