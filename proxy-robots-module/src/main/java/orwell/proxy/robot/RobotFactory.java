package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.elements.*;

import java.net.MalformedURLException;

/**
 * Created by Michaël Ludmann on 5/21/15.
 */
public final class RobotFactory {
    private final static Logger logback = LoggerFactory.getLogger(RobotFactory.class);

    public static IRobot getRobot(final IConfigRobot configRobot) throws ConfigRobotException {
        if (null == configRobot) {
            return null;
        }

        if (configRobot instanceof ConfigTank) {
            return getRobot((ConfigTank) configRobot);
        }
        if (configRobot instanceof ConfigScout) {
            return getRobot((ConfigScout) configRobot);
        }

        return null;
    }

    private static IRobot getRobot(final ConfigTank configTank) throws ConfigRobotException {
        switch (configTank.getEnumModel())
        {
            case EV3:
                return buildLegoEv3Tank(configTank);
            case NXT:
                return buildLegoNxtTank(configTank);
        }
        throw new ConfigRobotException(configTank, "EnumModel");
    }

    private static IRobot buildLegoEv3Tank(ConfigTank configTank) {
        return new LegoEv3Tank();
    }

    private static IRobot buildLegoNxtTank(ConfigTank configTank) {
        final IConfigCamera configCamera = configTank.getConfigCamera();
        if (null == configCamera) {
            logback.warn("Config of camera is missing for LegoNxtTank: " + configTank.getBluetoothName());
            logback.warn("Using dummy camera");
            return new LegoNxtTank(configTank.getBluetoothName(),
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
            return new LegoNxtTank(configTank.getBluetoothName(),
                    configTank.getBluetoothID(), ipWebcam, configTank.getImage());
        }
    }

    private static IRobot getRobot(final ConfigScout configScout) {
        logback.warn("Scout config is not handled yet");
        return null;
    }
}
