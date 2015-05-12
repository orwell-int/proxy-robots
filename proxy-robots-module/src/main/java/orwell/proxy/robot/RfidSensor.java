package orwell.proxy.robot;

import orwell.messages.Robot;

import java.util.ArrayDeque;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RfidSensor implements IRobotElement {

    private boolean isPreviousValueSet;
    private Robot.Rfid.Builder previousRead;

    public ArrayDeque<Robot.Rfid.Builder> getRfidSensorReads() {
        return rfidSensorReads;
    }

    private ArrayDeque<Robot.Rfid.Builder> rfidSensorReads;
    private final static String NO_RFID_VALUE = "0";


    public RfidSensor() {
        this.isPreviousValueSet = false;
    }


    private void setPreviousToOff(final Robot.Rfid.Builder previousRead) {
        if (null != previousRead) {
            this.isPreviousValueSet = true;
            final Robot.Rfid.Builder previousReadOff = previousRead;
            previousReadOff.setTimestamp(System.currentTimeMillis());
            previousReadOff.setStatus(Robot.Status.OFF);
            rfidSensorReads.addFirst(previousReadOff);
        }
    }

    public void setCurrentValue(final String currentValue) {
        setPreviousToOff(rfidSensorReads.peekFirst());

        if (0 != NO_RFID_VALUE.compareTo(currentValue)) {
            final Robot.Rfid.Builder builder = Robot.Rfid.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setStatus(Robot.Status.ON);
            builder.setRfid(currentValue);

            rfidSensorReads.addFirst(builder);
        }
    }



    public boolean isPreviousValueSet() {
        return isPreviousValueSet;
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }
}
