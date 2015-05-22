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
    private InputMove inputMove;
    private InputFire inputFire;

    public RobotInputSetVisitor(final byte[] inputMessage) {

        try {
            input = Controller.Input.parseFrom(inputMessage);
        } catch (final InvalidProtocolBufferException e) {
            logback.info("RobotActionSet protobuf exception: " + e.getMessage());
        }
    }

    public String inputToString(final IRobot robot) {
        final String string;
        if (null != input) {
            string = "Controller INPUT of Robot [" + robot.getRoutingId() + "]:"
                    + "\n\t|___Move order: [LEFT] "
                    + input.getMove().getLeft()
                    + " \t\t[RIGHT] "
                    + input.getMove().getRight()
                    + "\n\t|___Fire order: [WEAPON1] "
                    + input.getFire().getWeapon1()
                    + " \t[WEAPON2] "
                    + input.getFire().getWeapon2();
        } else {
            string = "Controller INPUT of Robot [" + robot.getRoutingId()
                    + "] NOT initialized!";
        }
        return string;
    }

    @Override
    public void visit(final InputMove inputMove) {

        logback.debug("Set move");
        this.inputMove = inputMove;
        if (input.hasMove()) {
            this.inputMove.setMove(input.getMove());
        }
    }

    @Override
    public void visit(final InputFire inputFire) {

        logback.debug("Set fire");
        this.inputFire = inputFire;
        if (!isEmpty(input)) {
            this.inputFire.setFire(input.getFire());
        }
    }

    /**
     * @param input
     * @return true is there is no relevant data inside input
     * (meaning it is not worth sending it to the robot)
     */
    private boolean isEmpty(final Controller.Input input) {
        return !(input.hasFire() &&
                (input.getFire().getWeapon1() || input.getFire().getWeapon2()));
    }


    @Override
    public void visit(final IRobot robot) {

        logback.debug("Set robot");
        if (null != this.inputMove && this.inputMove.hasMove()) {
            inputMove.sendUnitMessageTo(robot);
        }
        if (null != this.inputFire && this.inputFire.hasFire()) {
            inputFire.sendUnitMessageTo(robot);
        }
    }
}
