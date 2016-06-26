package orwell.proxy.config.elements;

import orwell.proxy.robot.EnumModel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class ConfigTank implements IConfigRobot {
    private String tempRoutingID;
    private String bluetoothName;
    private String bluetoothID;
    private ConfigCamera camera;
    private boolean shouldRegister;
    private String image;
    private List<ConfigNetworkInterface> configNetworkInterfaces;
    private String model;

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

    @Override
    public String getModel() {
        return model;
    }

    @Override
    @XmlAttribute
    public void setModel(final String model) {
        this.model = model;
    }

    @Override
    public EnumModel getEnumModel() {
        return EnumModel.getModelFromString(model);
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    @XmlElement
    public void setBluetoothName(final String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothID() {
        return bluetoothID;
    }

    @XmlElement
    public void setBluetoothID(final String bluetoothID) {
        this.bluetoothID = bluetoothID;
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

    @XmlElement(name = "networkInterface")
    public List<ConfigNetworkInterface> getConfigNetworkInterfaces() {
        return configNetworkInterfaces;
    }

    public void setConfigNetworkInterfaces(final List<ConfigNetworkInterface> configNetworkInterfaces) {
        this.configNetworkInterfaces = configNetworkInterfaces;
    }

    @Override
    public ConfigNetworkInterface getConfigNetworkInterface(String networkAddress) throws ConfigRobotException {
        for (final ConfigNetworkInterface config : this.configNetworkInterfaces) {
            if (config.getNetworkAddress().contentEquals(networkAddress))
                return config;
        }
        throw new ConfigRobotException(this, networkAddress);
    }
}
