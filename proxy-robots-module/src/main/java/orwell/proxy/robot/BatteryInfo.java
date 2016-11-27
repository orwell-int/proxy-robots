package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;

/**
 * Created by Michaël Ludmann on 27/11/16.
 */
public class BatteryInfo implements IRobotElement {
    private final static Logger logback = LoggerFactory.getLogger(BatteryInfo.class);
    private static final String BATTERY_MESSAGE_SPLIT_CHAR = " ";
    private static final int VOLTAGE_NO_VALUE = -1;
    private Robot.Battery currentBatteryValues;

    @Override
    public void accept(IRobotElementVisitor visitor) {
        visitor.visit(this);
    }

    public void setValue(String value) {
        logback.debug("Setting battery values: " + value);
        String[] values = value.split(BATTERY_MESSAGE_SPLIT_CHAR, 3);
        int voltageMilliVolt = Integer.parseInt(values[0]);
        float batteryCurrentAmps = Float.parseFloat(values[1]);
        float motorCurrentAmps = Float.parseFloat(values[2]);
        final Robot.Battery.Builder builder = Robot.Battery.newBuilder();
        builder.setTimestamp(System.currentTimeMillis());
        builder.setVoltageMilliVolt(voltageMilliVolt);
        builder.setBatteryCurrentAmps(batteryCurrentAmps);
        builder.setMotorCurrentAmps(motorCurrentAmps);
        currentBatteryValues = builder.build();
    }

    public Robot.Battery getBatteryValues() {
        if (currentBatteryValues == null) {
            final Robot.Battery.Builder builder = Robot.Battery.newBuilder();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setVoltageMilliVolt(VOLTAGE_NO_VALUE);
            currentBatteryValues = builder.build();
        }
        return currentBatteryValues;
    }

    @Override
    public String toString() {
        if (currentBatteryValues == null) {
            return "Battery info: no values";
        }
        return "Battery info: " + currentBatteryValues.getVoltageMilliVolt() + "mV " +
                currentBatteryValues.getBatteryCurrentAmps() + "A " +
                currentBatteryValues.getMotorCurrentAmps() + "A";
    }
}
