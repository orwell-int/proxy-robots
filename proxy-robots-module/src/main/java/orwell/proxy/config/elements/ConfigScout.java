package orwell.proxy.config.elements;

import orwell.proxy.robot.EnumModel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class ConfigScout implements IConfigRobot {

    private String tempRoutingID;
    private ConfigCamera camera;
    private boolean shouldRegister;
    private String image;
    private String model;
    private String hostname;

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

    @Override
    public IConfigNetworkInterface getConfigNetworkInterface(String address) {
        return null;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    @XmlAttribute
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public EnumModel getEnumModel() {
        return EnumModel.getModelFromString(model);
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    @XmlElement
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }
}
