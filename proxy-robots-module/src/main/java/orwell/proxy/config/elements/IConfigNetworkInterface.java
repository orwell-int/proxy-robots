package orwell.proxy.config.elements;

/**
 * Created by Michaël Ludmann on 23/06/16.
 */
public interface IConfigNetworkInterface {

    public String getMacAddress();

    public void setMacAddress(final String macAddress);

    public String getIpAddress();

    public void setIpAddress(final String ipAddress);

    public String getNetworkAddress();

    public void setNetworkAddress(final String networkAddress);
}
