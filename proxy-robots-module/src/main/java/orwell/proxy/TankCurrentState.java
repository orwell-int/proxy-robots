package orwell.proxy;

import orwell.messages.Robot;
import orwell.messages.Robot.Rfid;
import orwell.messages.Robot.Colour;
import orwell.messages.Robot.Status;
import orwell.messages.Robot.ServerRobotState;

/**
 * Proxy to ServerRobotState : handle only the last sensor value
 * read by the robot (not a list of the last values)
 */
public class TankCurrentState {

    private Rfid.Builder rfidBuilder = Rfid.newBuilder();
    private Colour.Builder colourBuilder = Colour.newBuilder();
    private Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
    private String previousRfidValue = null;
    private String previousColourValue = null;

    public TankCurrentState () {
    }

    public void setNewRfid(String newRfidString) {
        /**
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

        if (null == previousRfidValue) {
            rfidBuilder.setTimestamp(getTimeStamp());
            rfidBuilder.setStatus(Status.ON);
            rfidBuilder.setRfid(newRfidString);
            serverRobotStateBuilder.addRfid(rfidBuilder);

        } else if (previousRfidValue != newRfidString) {
            long currentTime = getTimeStamp();

            rfidBuilder.setTimestamp(currentTime);
            rfidBuilder.setStatus(Status.OFF);
            rfidBuilder.setRfid(previousRfidValue);
            serverRobotStateBuilder.addRfid(rfidBuilder);

            rfidBuilder.setStatus(Status.ON);
            rfidBuilder.setRfid(newRfidString);
            serverRobotStateBuilder.addRfid(rfidBuilder);
        }
        previousRfidValue = newRfidString;
    }

    public ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    public void clearServerRobotState() {
        serverRobotStateBuilder.clearRfid();
        serverRobotStateBuilder.clearColour();
    }

    public ServerRobotState getAndClearServerRobotState() {
        ServerRobotState srs = serverRobotStateBuilder.build();
        clearServerRobotState();
        return srs;
    }

    protected long getTimeStamp() {
        return System.currentTimeMillis();
    }
}
