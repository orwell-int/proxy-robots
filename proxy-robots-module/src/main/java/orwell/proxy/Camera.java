package orwell.proxy;

public class Camera implements ICamera {

    private String ip;
    private int port;

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
