package orwell.proxy.robot;

import lejos.mf.common.StreamUnitMessage;
import lejos.mf.common.UnitMessageType;
import orwell.messages.Controller.Input;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public class InputMove implements IRobotInput {

    private final static String MOVE_PAYLOAD_HEADER = "move ";
    private Input.Move move;
    private boolean hasMove = false;


    public void setMove(final Input.Move move) {
        this.move = move;
        this.hasMove = true;

    }


    public boolean hasMove() {
        return hasMove;
    }

    @Override
    public void accept(final IRobotInputVisitor visitor) {
        visitor.visit(this);
    }

    public void sendUnitMessageTo(final IRobot robot) {
        // "input move leftMove rightMove"
        robot.sendUnitMessage(new StreamUnitMessage(UnitMessageType.Command, MOVE_PAYLOAD_HEADER + move.getLeft() + " " + move.getRight()));
    }
}
