package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot.Register;

/**
 * Created by Michaël Ludmann on 5/19/15.
 */
public final class RegisterBytes {
    private final static Logger logback = LoggerFactory.getLogger(RegisterBytes.class);

    public static byte[] fromRobotFactory(final IRobot robot) {
        final Register.Builder registerBuilder = Register
                .newBuilder();
        final Register register;

        registerBuilder.setTemporaryRobotId(robot.getRoutingId());
        registerBuilder.setVideoUrl(robot.getCameraUrl());
        registerBuilder.setImage(robot.getImage());

        if (null == robot.getImage() ||
                0 == robot.getImage().compareTo("")) {
            logback.warn("Image of tank " + robot.getRoutingId() + " is empty. "
                    + "This will probably be an issue for the serverGame");
        }

        register = registerBuilder.build();
        if (null != register) {
            return register.toByteArray();
        } else {
            logback.error("Build of register failed");
            return null;
        }
    }
}