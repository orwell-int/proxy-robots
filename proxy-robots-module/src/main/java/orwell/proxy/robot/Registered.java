package orwell.proxy.robot;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.ServerGame;

/**
 * Created by Michaël Ludmann on 5/19/15.
 */
public class Registered {
    private final static Logger logback = LoggerFactory.getLogger(Registered.class);
    private ServerGame.Registered serverGameRegistered;

    public Registered(final byte[] registeredMessage) {
        try {
            this.serverGameRegistered = ServerGame.Registered.parseFrom(registeredMessage);
        } catch (final InvalidProtocolBufferException e) {
            logback.error("setRegistered protobuf exception: " + e.getMessage());
        }
    }

    public void setToRobot(final IRobot robot) {
        robot.setRoutingId(serverGameRegistered.getRobotId());
        if (robot.getRoutingId().isEmpty()) {
            robot.setRegistrationState(EnumRegistrationState.REGISTRATION_FAILED);
            logback.warn("Registration of robot: " + serverGameRegisteredToString(robot) + " FAILED");
        } else {
            robot.setRegistrationState(EnumRegistrationState.REGISTERED);
            robot.setTeamName(serverGameRegistered.getTeam());
            logback.info("Registered robot: " + serverGameRegisteredToString(robot));
        }
    }

    private String serverGameRegisteredToString(final IRobot robot) {
        final String string;
        if (null != serverGameRegistered) {
            string = "ServerGame REGISTERED of Robot [" + robot.getRoutingId() + "]:"
                    + "\n\t|___final RoutingID: "
                    + serverGameRegistered.getRobotId() + "\n\t|___team: "
                    + serverGameRegistered.getTeam();
        } else {
            string = "ServerGame REGISTERED of Robot [" + robot.getRoutingId()
                    + "] NOT initialized!";
        }
        return string;
    }
}
