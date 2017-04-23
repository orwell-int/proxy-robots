package orwell.proxy.robot;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;

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

    public String toString(final IRobot robot) {
        final String string;
        if (null != input) {
            string = "Controller INPUT of Robot [" + robot.getRoutingId() + "]:"
                    + " | Move order: [LEFT] " + input.getMove().getLeft()
                    + " [RIGHT] " + input.getMove().getRight()
                    + " | Fire order: [WEAPON1] " + input.getFire().getWeapon1()
                    + " [WEAPON2] " + input.getFire().getWeapon2();
        } else {
            string = "Controller INPUT of Robot [" + robot.getRoutingId()
                    + "] NOT initialized!";
        }
        return string;
    }

    @Override
    public void visit(final InputMove inputMove) {

        if (null == inputMove) {
            logback.warn("Empty inputMove");
            return;
        }
        this.inputMove = inputMove;
        if (null != input && input.hasMove()) {
            this.inputMove.setMove(input.getMove());
        }
    }

    @Override
    public void visit(final InputFire inputFire) {

        if (null == inputFire) {
            logback.warn("Empty inputFire");
            return;
        }
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
        return (null == input) || !(input.hasFire());
    }

    @Override
    public void visit(final IRobot robot) throws MessageNotSentException {

        if (null != this.inputMove && this.inputMove.hasMove()) {
            inputMove.sendUnitMessageTo(robot);
        }
        if (null != this.inputFire && this.inputFire.hasFire()) {
            inputFire.sendUnitMessageTo(robot);
        }
    }
}
