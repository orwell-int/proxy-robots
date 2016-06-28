package orwell.proxy.robot;

import lejos.mf.common.StreamUnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michaël Ludmann on 5/19/15.
 */
class UnitMessageBroker {
    private final static Logger logback = LoggerFactory.getLogger(UnitMessageBroker.class);
    private final IRobot robot;

    public UnitMessageBroker(final IRobot robot) {
        this.robot = robot;
    }

    public void handle(final StreamUnitMessage streamUnitMessage) {

        switch (streamUnitMessage.getMessageType()) {
            case Stop:
                onMsgStop();
                break;
            case Rfid:
                onMsgRfid(streamUnitMessage.getPayload());
                break;
            case Command:
                onMsgCommand(streamUnitMessage.getPayload());
                break;
            case Colour:
                onMsgColour(streamUnitMessage.getPayload());
                break;
            default:
                onMsgNotDefined(streamUnitMessage.getPayload());
                break;
        }
    }

    private void onMsgStop() {

        logback.info("Tank " + robot.getRoutingId() + " is stopping");
        robot.setConnectionState(EnumConnectionState.NOT_CONNECTED);
        robot.closeConnection();
    }

    private void onMsgRfid(final String rfidValue) {

        logback.debug("RFID info received: " + rfidValue);
        robot.setRfidValue(rfidValue);
    }

    private void onMsgColour(final String colourValue) {

        logback.debug("Colour info received: " + colourValue);
        robot.setColourValue(colourValue);
    }

    private void onMsgCommand(final String msg) {

        logback.debug("Tank is sending a command: " + msg);
        logback.debug("This command will not be processed");
    }

    private void onMsgNotDefined(final String msg) {

        logback.error("Unable to decode message received: " + msg);
    }

}
