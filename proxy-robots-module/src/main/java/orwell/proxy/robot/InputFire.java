package orwell.proxy.robot;

import lejos.mf.common.StreamUnitMessage;
import lejos.mf.common.UnitMessageType;
import orwell.messages.Controller;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public class InputFire implements IRobotInput {

    private final static String FIRE_PAYLOAD_HEADER = "fire ";
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
        // "input fire fireWeapon1 fireWeapon2"
        if (fire.getWeapon1() || fire.getWeapon2()) // We avoid flooding the robot if there is no fire
            try {
                robot.sendUnitMessage(
                        new StreamUnitMessage(
                                UnitMessageType.Command, FIRE_PAYLOAD_HEADER +
                                fire.getWeapon1() + " " + fire.getWeapon2())
                );
            } catch (MessageNotSentException e) {
                e.printStackTrace();
            }
    }
}
