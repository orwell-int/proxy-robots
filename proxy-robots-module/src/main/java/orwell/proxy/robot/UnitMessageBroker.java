package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitMessageBroker {
    private final static Logger logback = LoggerFactory.getLogger(UnitMessageBroker.class);
    private final IRobot robot;

    public UnitMessageBroker(final IRobot robot) {
        this.robot = robot;
    }

    public void handle(final UnitMessage unitMessage) {
        if (unitMessage == null)
            return;
        switch (unitMessage.getMessageType()) {
            case Stop:
                onMsgStop();
                break;
            case Command:
                onMsgCommand(unitMessage.getPayload());
                break;
            case Colour:
                onMsgColour(unitMessage.getPayload());
                break;
            case UltraSound:
                onMsgUltraSound(unitMessage.getPayload());
                break;
            case Battery:
                OnMsgBattery(unitMessage.getPayload());
                break;
            case Connection:
                onMsgConnection(unitMessage.getPayload());
                break;
            default:
                onMsgNotDefined(unitMessage.getPayload());
                break;
        }
    }

    private void onMsgStop() {

        logback.info("Tank " + robot.getRoutingId() + " is stopping");
        robot.setConnectionState(EnumConnectionState.NOT_CONNECTED);
        robot.closeConnection();
    }

    private void onMsgColour(final String colourValue) {

        logback.debug("Colour info received: " + colourValue);
        robot.setColourValue(colourValue);
    }

    private void onMsgCommand(final String msg) {

        logback.debug("Tank is sending a command: " + msg);
        logback.debug("This command will not be processed");
    }

    private void onMsgUltraSound(String usValue) {
        logback.debug("US info received: " + usValue);
        robot.setUsValue(Integer.parseInt(usValue));
    }

    private void OnMsgBattery(String batteryValues) {
        logback.debug("Battery info received: " + batteryValues);
        robot.setBatteryValues(batteryValues);
    }

    private void onMsgConnection(final String payload) {
        logback.debug("Tank sent a connection message: " + payload);
        if (payload.equalsIgnoreCase("connected")) {
            robot.setConnectionState(EnumConnectionState.CONNECTED);
        } else if (payload.equalsIgnoreCase("close")) {
            robot.setConnectionState(EnumConnectionState.NOT_CONNECTED);
        }
    }

    private void onMsgNotDefined(final String msg) {
        logback.error("Unable to decode message received: " + msg);
    }

}
