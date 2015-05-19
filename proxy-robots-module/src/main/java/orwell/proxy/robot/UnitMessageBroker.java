package orwell.proxy.robot;

import lejos.mf.common.UnitMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Michaël Ludmann on 5/19/15.
 */
public class UnitMessageBroker {
    private final static Logger logback = LoggerFactory.getLogger(UnitMessageBroker.class);
    private final LegoTank tank;

    public UnitMessageBroker(final LegoTank tank) {
        this.tank = tank;
    }

    public void handle(final UnitMessage unitMessage) {

        switch (unitMessage.getMsgType()) {
            case Stop:
                onMsgStop();
                break;
            case Rfid:
                onMsgRfid(unitMessage.getPayload());
                break;
            case Command:
                onMsgCommand(unitMessage.getPayload());
                break;
            case Colour:
                onMsgColour(unitMessage.getPayload());
                break;
            default:
                onMsgNotDefined(unitMessage.getPayload());
                break;
        }
    }

    private void onMsgStop() {

        logback.info("Tank " + tank.routingId + " is stopping");
        tank.connectionState = EnumConnectionState.NOT_CONNECTED;
        tank.closeConnection();
    }

    private void onMsgRfid(final String rfidValue) {

        logback.debug("RFID info received: " + rfidValue);
        tank.setRfidValue(rfidValue);
    }

    private void onMsgColour(final String colourValue) {

        logback.debug("Colour info received: " + colourValue);
        tank.setColourValue(colourValue);
    }

    private void onMsgCommand(final String msg) {

        logback.debug("Tank is sending a command: " + msg);
        logback.debug("This command will not be processed");
    }

    private void onMsgNotDefined(final String msg) {

        logback.error("Unable to decode message received: " + msg);
    }

}
