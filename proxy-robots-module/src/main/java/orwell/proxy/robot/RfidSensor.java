package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;

import java.util.ArrayDeque;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RfidSensor implements IRobotElement {
    private final static Logger logback = LoggerFactory.getLogger(RfidSensor.class);

    private final static String NO_RFID_VALUE = "0";
    private final ArrayDeque<Robot.Rfid> rfidSensorReads;

    public RfidSensor() {
        rfidSensorReads = new ArrayDeque<>();
    }

    public ArrayDeque<Robot.Rfid> getRfidSensorReads() {
        return rfidSensorReads;
    }

    private void setPreviousToOff() {
        final Robot.Rfid previousRead = rfidSensorReads.peekFirst();
        if (null != previousRead && Robot.Status.ON == previousRead.getStatus()) {
            final Robot.Rfid.Builder previousReadOff = previousRead.toBuilder();
            previousReadOff.setTimestamp(System.currentTimeMillis());
            previousReadOff.setStatus(Robot.Status.OFF);
            rfidSensorReads.addFirst(previousReadOff.build());
        }
    }

    /**
     * @param value String read by the sensor
     * @return true is the previous rfid value registered is the same
     * and its status is ON (Meaning it is still currently being read)
     */
    private boolean isPreviousIdentical(final String value) {
        final Robot.Rfid previousRead = rfidSensorReads.peekFirst();
        return (null != previousRead &&
                Robot.Status.ON == previousRead.getStatus() &&
                0 == value.compareTo(previousRead.getRfid()));
    }

    /**
     * Status marks the transition for a value in the table
     * If ON: this is the current value read by the robot
     * If OFF: this value is no longer read by the robot
     * <p/>
     * Previous value == null   |
     * |> Status x ON
     * Current value == x       |
     * <p/>
     * Previous value == x      |
     * |> Status x OFF, Status y ON
     * Current value == y       |
     * <p/>
     * Previous value == x      |
     * |> Status x ON, do nothing
     * Current value == x       |
     * <p/>
     * If currentValue is equal to NO_RFID_VALUE, it means the robot is
     * no longer reading Rfid values, so the previous value should
     * be set to OFF
     */
    public void setValue(final String currentValue) {
        logback.debug("Setting rfid value: " + currentValue);
        if (!isPreviousIdentical(currentValue)) {
            setPreviousToOff();

            if (0 != NO_RFID_VALUE.compareTo(currentValue)) {
                final Robot.Rfid.Builder builder = Robot.Rfid.newBuilder();
                builder.setTimestamp(System.currentTimeMillis());
                builder.setStatus(Robot.Status.ON);
                builder.setRfid(currentValue);

                rfidSensorReads.addFirst(builder.build());
            }
        }
    }


    public void clear() {
        logback.debug("Clearing all stored rfid values");
        this.rfidSensorReads.clear();
    }


    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }
}
