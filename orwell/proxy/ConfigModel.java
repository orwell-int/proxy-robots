package orwell.proxy;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="setup")
public class ConfigModel {
	
	private ConfigProxy configProxy;
	
	@XmlElement(name="proxy")
	public ConfigProxy getConfigProxy()
	{
		return configProxy;
	}
	
	public void setConfigProxy(ConfigProxy configProxy)
	{
		this.configProxy = configProxy;
	}
}

