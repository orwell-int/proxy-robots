package orwell.proxy.config.elements;

import orwell.proxy.robot.EnumModel;

/**
 * Created by Michaël Ludmann on 5/22/15.
 */
public interface IConfigRobot {

    String getTempRoutingID();

    void setTempRoutingID(final String tempRoutingID);

    boolean shouldRegister();

    void setShouldRegister(final boolean shouldRegister);

    String getImage();

    void setImage(final String image);

    IConfigNetworkInterface getConfigNetworkInterface(String address) throws ConfigRobotException;

    String getModel();

    void setModel(final String model);

    EnumModel getEnumModel();

    String getHostname();

    void setHostname(String hostname);
}
