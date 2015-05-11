package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by parapampa on 26/04/15.
 */
public class RobotsMap implements IRobotsMap {

    final static Logger logback = LoggerFactory.getLogger(RobotsMap.class);

    private final ArrayList<IRobot> array = new ArrayList<>();

    public RobotsMap() {

    }

    @Override
    public boolean add(IRobot robot) {
        if (this.array.contains(robot)) {
            logback.warn("Robot " + robot.getRoutingId() + " is already present in RobotsList");
            return false;
        } else {
            logback.debug("Adding robot " + robot.getRoutingId() + " to RobotsMap");
            return this.array.add(robot);
        }
    }

    @Override
    public boolean remove(String routingId) {
        IRobot robot = get(routingId);
        logback.debug("Removing robot " + routingId + " from RobotsMap");
        return this.array.remove(robot);
    }

    @Override
    public IRobot get(String routingId) {
        for (IRobot robot : this.array) {
            if (robot.getRoutingId().compareTo(routingId) == 0) {
                return robot;
            }
        }
        return null;
    }

    @Override
    public ArrayList<IRobot> getRobotsArray() {
        return this.array;
    }

    @Override
    public ArrayList<IRobot> getNotConnectedRobots() {
        ArrayList<IRobot> notConnectedRobotsList = new ArrayList<>();
        for (IRobot robot : this.array) {
            if (IRobot.EnumConnectionState.NOT_CONNECTED == robot.getConnectionState())
                notConnectedRobotsList.add(robot);
        }

        return notConnectedRobotsList;
    }

    @Override
    public ArrayList<IRobot> getConnectedRobots() {
        ArrayList<IRobot> connectedRobotsList = new ArrayList<>();
        for (IRobot robot : this.array) {
            if (IRobot.EnumConnectionState.CONNECTED == robot.getConnectionState())
                connectedRobotsList.add(robot);
        }

        return connectedRobotsList;
    }

    @Override
    public ArrayList<IRobot> getRegisteredRobots() {
        ArrayList<IRobot> registeredRobotsList = new ArrayList<>();
        for (IRobot robot : this.array) {
            if (IRobot.EnumRegistrationState.REGISTERED == robot.getRegistrationState())
                registeredRobotsList.add(robot);
        }

        return registeredRobotsList;
    }

    @Override
    public boolean isRobotConnected(String routingId) {
        IRobot robot = get(routingId);
        if (null == robot)
            return false;
        else
            return (IRobot.EnumConnectionState.CONNECTED == robot.getConnectionState());
    }

    @Override
    public boolean isRobotRegistered(String routingId) {
        IRobot robot = get(routingId);
        if (null == robot)
            return false;
        else
            return (IRobot.EnumRegistrationState.REGISTERED == robot.getRegistrationState());
    }


}
