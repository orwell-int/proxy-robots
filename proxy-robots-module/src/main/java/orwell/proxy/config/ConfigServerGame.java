package orwell.proxy.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "ip", "pushPort", "subPort" })
public class ConfigServerGame implements IConfigServerGame {

	private String name;
	private String ip;
	private int pushPort;
	private int subPort;

	@Override
	public String getName() {
		return name;
	}

	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getIp() {
		return ip;
	}

	@XmlElement
	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public int getPushPort() {
		return pushPort;
	}

	@XmlElement
	public void setPushPort(int pushPort) {
		this.pushPort = pushPort;
	}

	@Override
	public int getSubPort() {
		return subPort;
	}

	@XmlElement
	public void setSubPort(int subPort) {
		this.subPort = subPort;
	}

}
