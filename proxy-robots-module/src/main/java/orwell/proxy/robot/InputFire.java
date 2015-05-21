package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import orwell.messages.Controller;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public class InputFire implements IRobotInput {

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

    @Override
    public void accept(final IRobotInputVisitor visitor) {
        visitor.visit(this);
    }

    public void sendUnitMessageTo(final IRobot robot) {
        robot.sendUnitMessage(new UnitMessage(UnitMessageType.Command, FIRE_PAYLOAD_HEADER + fire.getWeapon1() + " " + fire.getWeapon2()));
    }
}
