package orwell.proxy.config.elements;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"camera", "image"})
public class ConfigScout implements IConfigRobot {

    private String tempRoutingID;
    private ConfigCamera camera;
    private boolean shouldRegister;
    private String image;

    @Override
    public String getTempRoutingID() {
        return tempRoutingID;
    }

    @Override
    @XmlAttribute
    public void setTempRoutingID(final String tempRoutingID) {
        this.tempRoutingID = tempRoutingID;
    }

    @Override
    public boolean shouldRegister() {
        return shouldRegister;
    }

    @Override
    @XmlAttribute
    public void setShouldRegister(final boolean shouldRegister) {
        this.shouldRegister = shouldRegister;
    }

    public ConfigCamera getConfigCamera() {
        return camera;
    }

    @XmlElement
    public void setCamera(final ConfigCamera camera) {
        this.camera = camera;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    @XmlElement
    public void setImage(final String image) {
        this.image = image;
    }
}
