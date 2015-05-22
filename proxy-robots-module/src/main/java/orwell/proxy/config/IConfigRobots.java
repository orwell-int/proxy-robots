package orwell.proxy.config;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public interface IConfigRobots {
    IConfigRobot getConfigRobot(String tempRoutingID) throws Exception;

    ArrayList<IConfigRobot> getConfigRobotsToRegister();
}
