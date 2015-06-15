package orwell.proxy.robot;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 26/04/15.
 */
public interface IRobotsMap {

    boolean add(IRobot robot);

    boolean remove(String routingId);

    IRobot get(String routingId);

    /**
     * @return all robots setup from the configuration
     */
    ArrayList<IRobot> getRobotsArray();

    /**
     * @return all robots not connected to the proxy
     */
    public ArrayList<IRobot> getNotConnectedRobots();

    /**
     * @return all robots connected to the proxy
     */
    ArrayList<IRobot> getConnectedRobots();

    /**
     * @return all robots registered on the server
     */
    ArrayList<IRobot> getRegisteredRobots();

    boolean isRobotConnected(String routingId);

    boolean isRobotRegistered(String routingId);

    void accept(RobotGameStateVisitor robotGameStateVisitor);
}
