package orwell.proxy.config.elements;

import orwell.proxy.robot.EnumModel;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Michaël Ludmann on 5/22/15.
 */
public interface IConfigRobot {

    public String getTempRoutingID();

    public void setTempRoutingID(final String tempRoutingID);

    public boolean shouldRegister();

    public void setShouldRegister(final boolean shouldRegister);

    public String getImage();

    public void setImage(final String image);

    public IConfigNetworkInterface getConfigNetworkInterface(String address) throws ConfigRobotException;

    public String getModel();

    public void setModel(final String model);

    EnumModel getEnumModel();

    String getHostname();

    @XmlElement
    void setHostname(String hostname);
}
