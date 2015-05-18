package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;

/**
 * Created by Michaël Ludmann on 5/18/15.
 */
public interface IRobot2 extends IRobotElement, IRobotInput {

    void sendInput(UnitMessage unitMessage);


}
