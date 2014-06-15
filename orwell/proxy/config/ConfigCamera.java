package orwell.proxy.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"ip", "port"})
public class ConfigCamera {
	private String ip;
	private int port;
	
	public String getIp()
	{
		return ip;
	}
	
	@XmlElement
	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getPort()
	{
		return port;
	}
	
	@XmlElement
	public void setPort(int port)
	{
		this.port = port;
	}

}
