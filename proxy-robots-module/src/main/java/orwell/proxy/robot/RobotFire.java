package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import orwell.messages.Controller;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public class RobotFire implements IRobotInput {

    private final static String FIRE_PAYLOAD_HEADER = "input fire ";
    private Controller.Input.Fire fire;
    private boolean hasFire = false;

    public void setFire(final Controller.Input.Fire fire) {
        this.fire = fire;
        this.hasFire = true;
    }

    public boolean hasFire() {
        return hasFire;
    }

    public UnitMessage getUnitMessage() {
        final StringBuilder stringBuilder = new StringBuilder(FIRE_PAYLOAD_HEADER);
        stringBuilder.append(fire.getWeapon1());
        stringBuilder.append(" ");
        stringBuilder.append(fire.getWeapon2());

        return new UnitMessage(UnitMessageType.Command, stringBuilder.toString());
    }

    @Override
    public void accept(final IRobotInputVisitor visitor) {
        visitor.visit(this);
    }
}
