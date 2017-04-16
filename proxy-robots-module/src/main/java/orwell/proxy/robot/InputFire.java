package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import lejos.mf.common.constants.UnitMessagePayloadHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;

public class InputFire implements IRobotInput {
    final static Logger logback = LoggerFactory.getLogger(IRobotInput.class);
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
                        new UnitMessage(
                                UnitMessageType.Command, UnitMessagePayloadHeaders.FireAction + " " +
                                fire.getWeapon1() + " " + fire.getWeapon2())
                );
            } catch (MessageNotSentException e) {
                logback.error(e.getMessage());
            }
    }
}
