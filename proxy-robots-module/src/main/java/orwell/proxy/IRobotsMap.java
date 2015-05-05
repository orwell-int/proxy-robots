package orwell.proxy;

import java.util.ArrayList;

/**
 * Created by parapampa on 26/04/15.
 */
public interface IRobotsMap {

    boolean add(IRobot robot);

    boolean remove(String routingId);

    IRobot get(String routingId);

    /*
     * @returns all robots setup from the configuration
     */
    ArrayList<IRobot> getRobotsArray();

    /*
     * @returns all robots not connected to the proxy
     */
    public ArrayList<IRobot> getNotConnectedRobots();

    /*
     * @returns all robots connected to the proxy
     */
    ArrayList<IRobot> getConnectedRobots();

    /*
     * @returns all robots registered on the server
     */
    ArrayList<IRobot> getRegisteredRobots();

    boolean isRobotConnected(String routingId);

    boolean isRobotRegistered(String routingId);

}
