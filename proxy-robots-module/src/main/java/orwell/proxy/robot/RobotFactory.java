package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCamera;
import orwell.proxy.config.ConfigTank;
import orwell.proxy.config.IConfigCamera;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
public final class RobotFactory {
    private final static Logger logback = LoggerFactory.getLogger(RobotFactory.class);

    public static LegoTank getLegoTank(final ConfigTank configTank) {
        if (null == configTank)
            return null;
        final IConfigCamera configCamera = configTank.getConfigCamera();
        if (null == configCamera) {
            logback.warn("Config of camera is missing for LegoTank: " + configTank.getBluetoothName());
            logback.warn("Using dummy camera");
            return new LegoTank(configTank.getBluetoothName(),
                    configTank.getBluetoothID(), IPWebcam.getDummy(), configTank.getImage());
        } else {
            final IPWebcam ipWebcam;
            try {
                ipWebcam = new IPWebcam(configCamera);
            } catch (final MalformedURLException e) {
                logback.error("Url of robot " + configTank.getBluetoothName() +
                        " is malformed. Robot will not be instantiated. " +
                        e.getMessage());
                return null;
            }
            //TODO Improve initialization of setImage to get something meaningful from the string (actual image)
            return new LegoTank(configTank.getBluetoothName(),
                    configTank.getBluetoothID(), ipWebcam, configTank.getImage());
        }
    }
}
