package orwell.proxy;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="setup")
public class ConfigModel {
	
	private ConfigProxy configProxy;
	private ConfigRobots configRobots;
	
	@XmlElement(name="proxy")
	public ConfigProxy getConfigProxy()
	{
		return configProxy;
	}
	
	public void setConfigProxy(ConfigProxy configProxy)
	{
		this.configProxy = configProxy;
	}
	
	@XmlElement(name="robots")
	public ConfigRobots getConfigRobots()
	{
		return configRobots;
	}
	
	public void setConfigRobots(ConfigRobots configRobots)
	{
		this.configRobots = configRobots;
	}
}

