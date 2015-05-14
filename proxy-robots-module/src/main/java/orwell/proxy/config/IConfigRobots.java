package orwell.proxy.config;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 03/05/15.
 */
public interface IConfigRobots {
    ConfigTank getConfigTank(String tempRoutingID) throws Exception;

    ArrayList<ConfigTank> getConfigRobotsToRegister();
}
