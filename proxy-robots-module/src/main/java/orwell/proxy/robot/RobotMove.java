package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import orwell.messages.Controller.Input;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public class RobotMove implements IRobotInput {

    private final static String MOVE_PAYLOAD_HEADER = "input move ";
    private Input.Move move;
    private boolean hasMove = false;


    public void setMove(final Input.Move move) {
        this.move = move;
        this.hasMove = true;

    }


    public boolean hasMove() {
        return hasMove;
    }


    public UnitMessage getUnitMessage() {
        final StringBuilder stringBuilder = new StringBuilder(MOVE_PAYLOAD_HEADER);
        stringBuilder.append(move.getLeft());
        stringBuilder.append(" ");
        stringBuilder.append(move.getRight());

        return new UnitMessage(UnitMessageType.Command, stringBuilder.toString());
    }


    @Override
    public void accept(final IRobotInputVisitor visitor) {
        visitor.visit(this);
    }
}
