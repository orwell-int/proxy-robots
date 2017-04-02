package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Common;

public class BatteryInfo implements IRobotElement {
    private final static Logger logback = LoggerFactory.getLogger(BatteryInfo.class);
    private static final String BATTERY_MESSAGE_SPLIT_CHAR = " ";
    private static final int VOLTAGE_NO_VALUE = -1;
    private Common.Battery currentBatteryValues;
    private boolean hasUpdate;
    private String lastValue = "";

    @Override
    public void accept(IRobotElementVisitor visitor) {
        visitor.visit(this);
    }

    public void setValue(String currentValue) {
        if (!lastValue.equals(currentValue)) {
            logback.debug("Setting battery values: " + currentValue);
            String[] values = currentValue.split(BATTERY_MESSAGE_SPLIT_CHAR, 3);
            int VoltageMillivolt = Integer.parseInt(values[0]);
            float currentAmps = Float.parseFloat(values[1]);
            final Common.Battery.Builder builder = Common.Battery.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setVoltageMillivolt(VoltageMillivolt);
            builder.setCurrentAmp(currentAmps);
            currentBatteryValues = builder.build();
            lastValue = currentValue;
            hasUpdate = true;
        }
    }

    public Common.Battery getBatteryValues() {
        if (currentBatteryValues == null) {
            final Common.Battery.Builder builder = Common.Battery.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setVoltageMillivolt(VOLTAGE_NO_VALUE);
            currentBatteryValues = builder.build();
        }
        hasUpdate = false;
        return currentBatteryValues;
    }

    @Override
    public String toString() {
        if (currentBatteryValues == null) {
            return "Battery info: no values";
        }
        return "Battery info: " + currentBatteryValues.getVoltageMillivolt() + "mV " +
                currentBatteryValues.getCurrentAmp() + "A";
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }
}
