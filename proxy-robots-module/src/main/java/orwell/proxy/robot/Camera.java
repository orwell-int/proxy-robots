package orwell.proxy.robot;

public class Camera implements ICamera {

    private final String ip;
    private final int port;

    public Camera(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String getUrl() {
        return "http://" + ip + ":" + port + "/videofeed";
    }
}
