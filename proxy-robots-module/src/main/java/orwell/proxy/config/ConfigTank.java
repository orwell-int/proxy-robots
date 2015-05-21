package orwell.proxy.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"bluetoothName", "bluetoothID", "camera", "image"})
public class ConfigTank {

    private String tempRoutingID;
    private String bluetoothName;
    private String bluetoothID;
    private IConfigCamera camera;
    private boolean shouldRegister;
    private String image;

    public String getTempRoutingID() {
        return tempRoutingID;
    }

    @XmlAttribute
    public void setTempRoutingID(final String tempRoutingID) {
        this.tempRoutingID = tempRoutingID;
    }

    public boolean shouldRegister() {
        return shouldRegister;
    }

    @XmlAttribute
    public void setShouldRegister(final boolean shouldRegister) {
        this.shouldRegister = shouldRegister;
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

    public IConfigCamera getConfigCamera() {
        return camera;
    }

    @XmlElement
    public void setCamera(final IConfigCamera camera) {
        this.camera = camera;
    }

    public String getImage() {
        return image;
    }

    @XmlElement
    public void setImage(final String image) {
        this.image = image;
    }
}
