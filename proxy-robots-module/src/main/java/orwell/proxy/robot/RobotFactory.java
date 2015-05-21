package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigCamera;
import orwell.proxy.config.ConfigTank;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
public final class RobotFactory {
    private final static Logger logback = LoggerFactory.getLogger(RobotFactory.class);

    public static LegoTank getLegoTank(final ConfigTank configTank) {
        final ConfigCamera configCamera = configTank.getConfigCamera();
        if (null == configCamera) {
            logback.error("Config of camera is missing for LegoTank: " + configTank.getBluetoothName());
            return null;
        } else {
            final IPWebcam ipWebcam = new IPWebcam(configCamera);
            //TODO Improve initialization of setImage to get something meaningful from the string (actual image)
            return new LegoTank(configTank.getBluetoothName(),
                    configTank.getBluetoothID(), ipWebcam, configTank.getImage());
        }
    }
}
