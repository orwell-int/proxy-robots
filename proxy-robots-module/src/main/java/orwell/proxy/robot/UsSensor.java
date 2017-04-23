package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Common;

public class UsSensor implements IRobotElement {
    private final static Logger logback = LoggerFactory.getLogger(UsSensor.class);

    private Common.Ultrasound currentUltrasoundValue;
    private boolean hasUpdate = false;
    private int lastValue;

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }

    public void setValue(final int currentValue) {
        if (currentValue != lastValue) {
            logback.debug("Setting US value: " + currentValue);
            final Common.Ultrasound.Builder builder = Common.Ultrasound.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setDistance(currentValue);
            this.currentUltrasoundValue = builder.build();
            lastValue = currentValue;
            hasUpdate = true;
        }
    }

    public Common.Ultrasound getUltrasoundRead() {
        if (currentUltrasoundValue == null) {
            final Common.Ultrasound.Builder builder = Common.Ultrasound.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setDistance(Integer.MAX_VALUE);
            currentUltrasoundValue = builder.build();
        }
        hasUpdate = false;
        return currentUltrasoundValue;
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    @Override
    public String toString() {
        if (currentUltrasoundValue == null) {
            return "UsSensor: no value";
        }
        return "UsSensor: " + currentUltrasoundValue.getDistance();
    }
}
