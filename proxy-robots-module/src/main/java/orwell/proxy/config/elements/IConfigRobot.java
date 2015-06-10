package orwell.proxy.config.elements;

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
}
