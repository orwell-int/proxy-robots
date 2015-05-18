package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;

import java.util.ArrayDeque;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class ColourSensor implements IRobotElement {
    private final static Logger logback = LoggerFactory.getLogger(ColourSensor.class);

    private final ArrayDeque<Robot.Colour> colourSensorReads;

    public ColourSensor() {
        colourSensorReads = new ArrayDeque<>();
    }

    public ArrayDeque<Robot.Colour> getColourSensorReads() {
        return colourSensorReads;
    }

    private void setPreviousToOff() {
        final Robot.Colour previousRead = colourSensorReads.peekFirst();
        if (null != previousRead && Robot.Status.ON == previousRead.getStatus()) {
            final Robot.Colour.Builder previousReadOff = previousRead.toBuilder();
            previousReadOff.setTimestamp(System.currentTimeMillis());
            previousReadOff.setStatus(Robot.Status.OFF);
            colourSensorReads.addFirst(previousReadOff.build());
        }
    }

    /**
     * @param value String read by the sensor
     * @return true is the previous Colour value registered is the same
     * and its status is ON (Meaning it is still currently being read)
     */
    private boolean isPreviousIdentical(final String value) {
        final Robot.Colour previousRead = colourSensorReads.peekFirst();
        return (null != previousRead &&
                Robot.Status.ON == previousRead.getStatus() &&
                previousRead.getColour() == Integer.parseInt(value));
    }

    public void setValue(final String currentValue) {
        logback.debug("Setting Colour value: " + currentValue);
        if (!isPreviousIdentical(currentValue)) {
            setPreviousToOff();

            final Robot.Colour.Builder builder = Robot.Colour.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setStatus(Robot.Status.ON);
            builder.setColour(Integer.parseInt(currentValue));

            colourSensorReads.addFirst(builder.build());
        }
    }


    public void clear() {
        logback.debug("Clearing all stored colour values");
        this.colourSensorReads.clear();
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }
}
