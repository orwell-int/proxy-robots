package orwell.proxy;

import com.google.protobuf.MessageLiteOrBuilder;
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
    final static Logger logback = LoggerFactory.getLogger(TankDeltaState.class);

    private Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
    private HashMap<EnumSensor, ISensorWrapper> previousStateMap;

    public TankDeltaState() {
        logback.info("Creating a new TankCurrentState");
        initPreviousStateMap();
    }

    private void initPreviousStateMap() {
        previousStateMap = new HashMap<EnumSensor, ISensorWrapper>(2);
        previousStateMap.put(EnumSensor.RFID, new RfidWrapper());
        previousStateMap.put(EnumSensor.COLOUR, new ColourWrapper());
    }

    private void setNewRfid(String newRfidString) {
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
        String previousValue = previousStateMap.get(EnumSensor.RFID).getPreviousValue();
        Rfid.Builder builder = (Rfid.Builder) previousStateMap.get(EnumSensor.RFID).getBuilder();

        if (null == previousValue) {
            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.ON);
            builder.setRfid(newRfidString);
            serverRobotStateBuilder.addRfid(builder);

        } else if (previousValue != newRfidString) {
            long currentTime = getTimeStamp();

            builder.setTimestamp(currentTime);
            builder.setStatus(Status.OFF);
            builder.setRfid(previousValue);
            serverRobotStateBuilder.addRfid(builder);

            builder.setStatus(Status.ON);
            builder.setRfid(newRfidString);
            serverRobotStateBuilder.addRfid(builder);
        }
    }

    private void setNewColour(String newColourString) {
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
        int newColourInt = Integer.parseInt(newColourString);
        int previousValue = ((ColourWrapper) previousStateMap.get(EnumSensor.COLOUR)).getPreviousValueInteger();
        Colour.Builder builder = (Colour.Builder) previousStateMap.get(EnumSensor.COLOUR).getBuilder();

        if (-1 == previousValue) {
            builder.setTimestamp(getTimeStamp());
            builder.setStatus(Status.ON);
            builder.setColour(newColourInt);
            serverRobotStateBuilder.addColour(builder);

        } else if (previousValue != newColourInt) {
            long currentTime = getTimeStamp();

            builder.setTimestamp(currentTime);
            builder.setStatus(Status.OFF);
            builder.setColour(previousValue);
            serverRobotStateBuilder.addColour(builder);

            builder.setStatus(Status.ON);
            builder.setColour(newColourInt);
            serverRobotStateBuilder.addColour(builder);
        }
    }

    public void setNewState(EnumSensor enumSensor, String newState) {
        MessageLiteOrBuilder builder;
        switch (enumSensor) {
            case RFID:
                setNewRfid(newState);
                break;
            case COLOUR:
                setNewColour(newState);
                break;
            default:
                logback.warn("Sensor not handled: " + enumSensor);
                return;
        }
        previousStateMap.get(enumSensor).setPreviousValue(newState);
    }

    protected ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    public void clearServerRobotState() {
        serverRobotStateBuilder.clearRfid();
        serverRobotStateBuilder.clearColour();
    }

    public ServerRobotState getAndClearServerRobotState() {
        if (serverRobotStateBuilder.getRfidList().isEmpty() &&
                serverRobotStateBuilder.getColourList().isEmpty()) {
            return null;
        }
        ServerRobotState srs = serverRobotStateBuilder.build();
        clearServerRobotState();
        return srs;
    }

    protected long getTimeStamp() {
        return System.currentTimeMillis();
    }
}
