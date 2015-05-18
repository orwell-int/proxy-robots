package orwell.proxy.robot;

import lejos.mf.common.MessageListenerInterface;
import lejos.mf.common.UnitMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Tank2 implements IRobot2, MessageListenerInterface {
    private final static Logger logback = LoggerFactory.getLogger(Tank2.class);
    private IRobotElement[] robotElements;
    private IRobotInput[] robotActions;



    public Tank2(final ICamera camera) {
        this.robotElements = new IRobotElement[] {camera, new RfidSensor(), new ColourSensor()};
        this.robotActions = new IRobotInput[] {new RobotMove(), new RobotFire()};
    }


    public void setRfidValue(final String rfidValue) {
        ((RfidSensor) robotElements[1]).setValue(rfidValue);
    }

    public void setColourValue(final String colourValue) {
        ((ColourSensor) robotElements[2]).setValue(colourValue);
    }


    @Override
    public void receivedNewMessage(final UnitMessage msg) {

    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        for(final IRobotElement element: robotElements) {
            element.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void accept(final IRobotInputVisitor visitor) {
        for(final IRobotInput action: robotActions) {
            action.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void sendInput(final UnitMessage unitMessage) {

        logback.debug("Sending input to physical device");

    }
}
