package orwell.proxy.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "bluetoothName", "bluetoothID", "camera" })
public class ConfigTank {

	private String routingID;
	private String bluetoothName;
	private String bluetoothID;
	private ConfigCamera camera;

	public String getRoutingID() {
		return routingID;
	}

	@XmlAttribute
	public void setRoutingID(String routingID) {
		this.routingID = routingID;
	}

	public String getBluetoothName() {
		return bluetoothName;
	}

	@XmlElement
	public void setBluetoothName(String bluetoothName) {
		this.bluetoothName = bluetoothName;
	}

	public String getBluetoothID() {
		return bluetoothID;
	}

	@XmlElement
	public void setBluetoothID(String bluetoothID) {
		this.bluetoothID = bluetoothID;
	}

	public ConfigCamera getConfigCamera() {
		return camera;
	}

	@XmlElement
	public void setCamera(ConfigCamera camera) {
		this.camera = camera;
	}
}
