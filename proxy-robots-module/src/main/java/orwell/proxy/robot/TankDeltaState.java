package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Robot;
import orwell.messages.Robot.Colour;
import orwell.messages.Robot.Rfid;
import orwell.messages.Robot.ServerRobotState;
import orwell.messages.Robot.Status;

import java.util.HashMap;

/**
 * Proxy to ServerRobotState : handle only the last sensor value
 * read by the robot (not a list of the last values)
 */
public class TankDeltaState {
    private final static Logger logback = LoggerFactory.getLogger(TankDeltaState.class);
    private final static String NO_RFID_VALUE = "0";

    private final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
    private HashMap<EnumSensor, ISensorWrapper> previousStateMap;

    public TankDeltaState() {
        logback.info("Creating a new TankCurrentState");
        initPreviousStateMap();
    }

    private void initPreviousStateMap() {
        previousStateMap = new HashMap<>(2);
        previousStateMap.put(EnumSensor.RFID, new RfidWrapper());
        previousStateMap.put(EnumSensor.COLOUR, new ColourWrapper());
    }

    private void setNewRfid(final String newRfidString) {
        /**
         * Status marks the transition for a value in the table
         * If ON: this is the current value read by the robot
         * If OFF: this value is no longer read by the robot
         *
         * Previous value == null  |
         *                         |> Status x ON
         * Current value == x      |
         *
         * Previous value == x     |
         *                         |> Status x OFF, Status y ON
         * Current value == y      |
         *
         * Previous value == x     |
         *                         |> Status x ON, do nothing
         * Current value == x      |
         *
         * If newRfidString is equal to NO_RFID_VALUE, it means the robot is
         * no longer reading Rfid values, so the previous value should
         * be set to OFF
         */
        final String previousValue = previousStateMap.get(EnumSensor.RFID).getPreviousValue();
        final Rfid.Builder builder = (Rfid.Builder) previousStateMap.get(EnumSensor.RFID).getBuilder();

        // if previousValue is not initialised and the new value is NO_RFID_VALUE
        // then there is nothing to do
        if (null == previousValue && 0 == newRfidString.compareTo(NO_RFID_VALUE)) {
            return;
        }
        // If this is the first read, we initialise the builder
        if (null == previousValue) {
            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.ON);
            builder.setRfid(newRfidString);
            serverRobotStateBuilder.addRfid(builder);
            // else we register the transition
        } else if (0 != previousValue.compareTo(newRfidString)) {
            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.OFF);
            builder.setRfid(previousValue);
            serverRobotStateBuilder.addRfid(builder);

            // If the tank reads NO_RFID_VALUE, we do not register
            // it as a value in itself
            if (0 != newRfidString.compareTo(NO_RFID_VALUE)) {
                builder.setTimestamp(getTimeStamp());
                builder.setStatus(Status.ON);
                builder.setRfid(newRfidString);
                serverRobotStateBuilder.addRfid(builder);
            }
        }
    }

    private void setNewColour(final String newColourString) {
        /**
         * Status marks the transition for a value in the table
         * If ON: this is the current value read by the robot
         * If OFF: this value is no longer read by the robot
         *
         * Previous value == null  |
         *                         |> Status x ON
         * Current value == x      |
         *
         * Previous value == x     |
         *                         |> Status x OFF, Status y ON
         * Current value == y      |
         *
         * Previous value == x     |
         *                         |> Status x ON, do nothing
         * Current value == x      |
         */
        final int newColourInt = Integer.parseInt(newColourString);
        final int previousValue = ((ColourWrapper) previousStateMap.get(EnumSensor.COLOUR)).getPreviousValueInteger();
        final Colour.Builder builder = (Colour.Builder) previousStateMap.get(EnumSensor.COLOUR).getBuilder();

        if (ColourWrapper.INIT_VALUE == previousValue) {
            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.ON);
            builder.setColour(newColourInt);
            serverRobotStateBuilder.addColour(builder);

        } else if (previousValue != newColourInt) {
            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.OFF);
            builder.setColour(previousValue);
            serverRobotStateBuilder.addColour(builder);

            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.ON);
            builder.setColour(newColourInt);
            serverRobotStateBuilder.addColour(builder);
        }
    }

    public void setNewState(final EnumSensor enumSensor, final String newState) {
        switch (enumSensor) {
            case RFID:
                setNewRfid(newState);
                // We do not register NO_RFID_VALUE
                if (0 != newState.compareTo(NO_RFID_VALUE)) {
                    previousStateMap.get(enumSensor).setPreviousValue(newState);
                }
                break;
            case COLOUR:
                setNewColour(newState);
                previousStateMap.get(enumSensor).setPreviousValue(newState);
                break;
            default:
                logback.warn("Sensor not handled: " + enumSensor);
        }
    }

    /**
     * This will not clear the delta state,
     * future access will continue to build on the previous state
     *
     * @return latest ServerRobotState
     */
    public ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    private void clearServerRobotState() {
        serverRobotStateBuilder.clearRfid();
        serverRobotStateBuilder.clearColour();
    }

    /**
     * This will clear the ServerRobotState jut got by the method
     * so as to build a real delta state
     * This should be used in a normal real-time run
     *
     * @return latest ServerRobotState
     */
    public ServerRobotState getServerRobotState_And_ClearDelta() {
        if (serverRobotStateBuilder.getRfidList().isEmpty() &&
                serverRobotStateBuilder.getColourList().isEmpty()) {
            return null;
        }
        final ServerRobotState srs = serverRobotStateBuilder.build();
        clearServerRobotState();
        return srs;
    }

    private long getTimeStamp() {
        return System.currentTimeMillis();
    }
}
