package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Michaël Ludmann on 26/04/15.
 */
public class RobotsMap implements IRobotsMap {
    private final static Logger logback = LoggerFactory.getLogger(RobotsMap.class);

    private final ArrayList<IRobot> array = new ArrayList<>();

    public RobotsMap() {

    }

    @Override
    public boolean add(final IRobot robot) {
        if (this.array.contains(robot)) {
            logback.warn("Robot " + robot.getRoutingId() + " is already present in RobotsList");
            return false;
        } else {
            logback.debug("Adding robot " + robot.getRoutingId() + " to RobotsMap");
            return this.array.add(robot);
        }
    }

    @Override
    public boolean remove(final String routingId) {
        final IRobot robot = get(routingId);
        logback.debug("Removing robot " + routingId + " from RobotsMap");
        return this.array.remove(robot);
    }

    @Override
    public IRobot get(final String routingId) {
        for (final IRobot robot : this.array) {
            if (0 == robot.getRoutingId().compareTo(routingId)) {
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
        final ArrayList<IRobot> notConnectedRobotsList = new ArrayList<>();
        for (final IRobot robot : this.array) {
            if (IRobot.EnumConnectionState.NOT_CONNECTED == robot.getConnectionState())
                notConnectedRobotsList.add(robot);
        }

        return notConnectedRobotsList;
    }

    @Override
    public ArrayList<IRobot> getConnectedRobots() {
        final ArrayList<IRobot> connectedRobotsList = new ArrayList<>();
        for (final IRobot robot : this.array) {
            if (IRobot.EnumConnectionState.CONNECTED == robot.getConnectionState())
                connectedRobotsList.add(robot);
        }

        return connectedRobotsList;
    }

    @Override
    public ArrayList<IRobot> getRegisteredRobots() {
        final ArrayList<IRobot> registeredRobotsList = new ArrayList<>();
        for (final IRobot robot : this.array) {
            if (IRobot.EnumRegistrationState.REGISTERED == robot.getRegistrationState())
                registeredRobotsList.add(robot);
        }

        return registeredRobotsList;
    }

    @Override
    public boolean isRobotConnected(final String routingId) {
        final IRobot robot = get(routingId);
        if (null == robot)
            return false;
        else
            return (IRobot.EnumConnectionState.CONNECTED == robot.getConnectionState());
    }

    @Override
    public boolean isRobotRegistered(final String routingId) {
        final IRobot robot = get(routingId);
        if (null == robot)
            return false;
        else
            return (IRobot.EnumRegistrationState.REGISTERED == robot.getRegistrationState());
    }
}
