package orwell.proxy.robot;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;


/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RobotInputSetVisitor implements IRobotInputVisitor {
    private final static Logger logback = LoggerFactory.getLogger(RobotInputSetVisitor.class);
    private Controller.Input input;
    private RobotMove robotMove;
    private RobotFire robotFire;

    public RobotInputSetVisitor(final byte[] inputMessage) {

        try {
            input = Controller.Input.parseFrom(inputMessage);
        } catch (final InvalidProtocolBufferException e) {
            logback.info("RobotActionSet protobuf exception: " + e.getMessage());
        }
    }

    public String inputToString(final IRobot2 robot) {
        final String string;
        if (null != input) {
            string = "Controller INPUT of Robot [" + robot.routingId + "]:"
                    + "\n\t|___Move order: [LEFT] "
                    + input.getMove().getLeft()
                    + " \t\t[RIGHT] "
                    + input.getMove().getRight()
                    + "\n\t|___Fire order: [WEAPON1] "
                    + input.getFire().getWeapon1()
                    + " \t[WEAPON2] "
                    + input.getFire().getWeapon2();
        } else {
            string = "Controller INPUT of Robot [" + robot.routingId
                    + "] NOT initialized!";
        }
        return string;
    }

    @Override
    public void visit(final RobotMove robotMove) {

        logback.debug("Set move");
        this.robotMove = robotMove;
        if(input.hasMove()) {
            this.robotMove.setMove(input.getMove());
        }
    }

    @Override
    public void visit(final RobotFire robotFire) {

        logback.debug("Set fire");
        this.robotFire = robotFire;
        if(input.hasFire() &&
                (input.getFire().getWeapon1() || input.getFire().getWeapon2()))
        {
            this.robotFire.setFire(input.getFire());
        }
    }


    @Override
    public void visit(final IRobot2 robot) {

        logback.debug("Set robot");
        if(null != this.robotMove && this.robotMove.hasMove()) {
            robotMove.sendUnitMessageTo(robot);
        }
        if(null != this.robotFire && this.robotFire.hasFire()) {
            robotFire.sendUnitMessageTo(robot);
        }
    }
}
