package orwell.proxy.robot;

public class Camera implements ICamera {

    private final String ip;
    private final int port;

    public Camera(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public final static ICamera getMock() {
        class MockCamera implements ICamera {

            @Override
            public String getUrl() {
                return "http://fake.url";
            }
        }

        MockCamera camera = new MockCamera();
        return camera;
    }

    @Override
    public String getUrl() {
        return "http://" + ip + ":" + port + "/videofeed";
    }
}
