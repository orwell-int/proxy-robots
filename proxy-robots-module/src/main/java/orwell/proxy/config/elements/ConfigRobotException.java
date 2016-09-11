package orwell.proxy.config.elements;

/**
 * Created by Michaël Ludmann on 26/06/16.
 */
public class ConfigRobotException extends Exception {
    final private IConfigRobot configRobot;
    final private String missingField;

    public ConfigRobotException(ConfigTank configTank, String missingField) {
        configRobot = configTank;
        this.missingField = missingField;
    }

    public String getMessage() {
        return "[Configuration File] " + configRobot.getClass() +
                " is missing mandatory field: " + missingField;
    }
}
