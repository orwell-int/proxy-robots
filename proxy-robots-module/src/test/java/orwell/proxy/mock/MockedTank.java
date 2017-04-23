package orwell.proxy.mock;

import lejos.mf.common.UnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.robot.*;

public class MockedTank extends IRobot {
    private final static Logger logback = LoggerFactory.getLogger(MockedTank.class);

    private final IRobotElement[] robotElements;
    private final IRobotInput[] robotActions;
    private boolean isAbleToSendUnitMessage;

    public MockedTank() {
        this.setRoutingId("tempRoutingId");
        this.setCameraUrl("http://fake.url");
        this.setImage("noImage");
        this.setRegistrationState(EnumRegistrationState.NOT_REGISTERED);
        this.setConnectionState(EnumConnectionState.NOT_CONNECTED);
        this.setTeamName("BLUE");
        this.robotElements = new IRobotElement[]{new UsSensor(), new ColourSensor()};
        this.robotActions = new IRobotInput[]{new InputMove(), new InputFire()};
        isAbleToSendUnitMessage = true;
    }

    @Override
    public void setUsValue(int usValue) {
        ((UsSensor) robotElements[0]).setValue(usValue);
    }

    @Override
    public void setBatteryValues(String batteryValue) {

    }

    @Override
    public void setColourValue(String colourValue) {
        ((ColourSensor) robotElements[1]).setValue(colourValue);
    }

    public InputMove getInputMove() {
        return (InputMove) robotActions[0];
    }

    public InputFire getInputFire() {
        return (InputFire) robotActions[1];
    }

    @Override
    public void sendUnitMessage(final UnitMessage unitMessage) throws MessageNotSentException {
        if (!isAbleToSendUnitMessage()) {
            throw new MessageNotSentException("MockedTank");
        }
    }

    private boolean isAbleToSendUnitMessage() {
        return isAbleToSendUnitMessage;
    }

    @Override
    public EnumConnectionState connect() {
        logback.info("Connecting to physical device");
        setConnectionState(EnumConnectionState.CONNECTED);
        return getConnectionState();
    }

    @Override
    public void closeConnection() {
        logback.info("Closing connection to physical device");
        setConnectionState(EnumConnectionState.NOT_CONNECTED);
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        for (final IRobotElement element : robotElements) {
            element.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public void accept(final IRobotInputVisitor visitor) throws MessageNotSentException {
        for (final IRobotInput action : robotActions) {
            action.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MockedTank { [RoutingID] " + getRoutingId() +
                " [TeamName] " + getTeamName() + " }";
    }

    public void makeUnableToSendUnitMessages() {
        isAbleToSendUnitMessage = false;
    }
}
