package orwell.proxy;

public class Camera {

	private String ip;
	private int port;

	public Camera(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getURL() {
		return "http://" + ip + ":" + port + "/videofeed";
	}
}
