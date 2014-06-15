package orwell.proxy.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"bluetoothName", "bluetoothID", "camera"})
public class ConfigTank {

	private String networkID;
	private String bluetoothName;
	private String bluetoothID;
	private ConfigCamera camera;
	
	public String getNetworkID()
	{
		return networkID;
	}
	
	@XmlAttribute
	public void setNetworkID(String networkID)
	{
		this.networkID = networkID;
	}
	
	public String getBluetoothName()
	{
		return bluetoothName;
	}
	
	@XmlElement
	public void setBluetoothName(String bluetoothName)
	{
		this.bluetoothName = bluetoothName;
	}
	
	public String getBluetoothID()
	{
		return bluetoothID;
	}
	
	@XmlElement
	public void setBluetoothID(String bluetoothID)
	{
		this.bluetoothID = bluetoothID;
	}
	
	public ConfigCamera getConfigCamera()
	{
		return camera;
	}
	
	@XmlElement
	public void setCamera(ConfigCamera camera)
	{
		this.camera = camera;
	}
}
