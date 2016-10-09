package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;

/**
 * Created by Michaël Ludmann on 12/09/16.
 */
public class UsSensor implements IRobotElement {
    private final static Logger logback = LoggerFactory.getLogger(UsSensor.class);
    public static final float US_NO_VALUE = -1;

    private Robot.Ultrasound currentUltrasoundValue;

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }

    public void setValue(final float currentValue) {
        logback.debug("Setting US value: " + currentValue);
        final Robot.Ultrasound.Builder builder = Robot.Ultrasound.newBuilder();
        builder.setTimestamp(System.currentTimeMillis());
        builder.setUltrasound(currentValue);
        this.currentUltrasoundValue = builder.build();
    }

    public Robot.Ultrasound getUltrasoundRead() {
        if (currentUltrasoundValue == null) {
            final Robot.Ultrasound.Builder builder = Robot.Ultrasound.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setUltrasound(US_NO_VALUE);
            currentUltrasoundValue = builder.build();
        }
        return currentUltrasoundValue;
    }

    @Override
    public String toString() {
        if (currentUltrasoundValue == null) {
            return "UsSensor: no value";
        }
        return "UsSensor: " + currentUltrasoundValue.getUltrasound();
    }
}
