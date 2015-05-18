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
                (input.getFire().getWeapon1() ||input.getFire().getWeapon2()))
        {
            this.robotFire.setFire(input.getFire());
        }
    }


    @Override
    public void visit(final IRobot2 robot) {

        logback.debug("Set robot");
        if(this.robotMove.hasMove())
            robot.sendInput(robotMove.getUnitMessage());
        if(this.robotFire.hasFire())
            robot.sendInput(robotFire.getUnitMessage());
    }
}
