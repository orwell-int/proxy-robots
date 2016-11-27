package orwell.proxy.robot;

import orwell.messages.Robot;


/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class RobotElementStateVisitor implements IRobotElementVisitor {
    private final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();

    protected Robot.ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    /**
     * @return the byte array of ServerRobotState or
     * null if rfid and colour list are empty
     */
    public byte[] getServerRobotStateBytes() {
        final Robot.ServerRobotState serverRobotState = getServerRobotState();
        if (isServerRobotStateEmpty(serverRobotState)) {
            return null;
        }
        else {
            return serverRobotState.toByteArray();
        }
    }

    private boolean isServerRobotStateEmpty(Robot.ServerRobotState serverRobotState) {
        return null == serverRobotState ||
                (serverRobotState.getRfidList().isEmpty() &&
                        serverRobotState.getColourList().isEmpty() &&
                        Float.compare(serverRobotState.getUltrasound().getUltrasound(), UsSensor.US_NO_VALUE) == 0
                );
    }

    @Override
    public void visit(final RfidSensor rfidSensor) {
        serverRobotStateBuilder.addAllRfid(rfidSensor.getRfidSensorReads());
        rfidSensor.clear();
    }

    @Override
    public void visit(final ColourSensor colourSensor) {
        serverRobotStateBuilder.addAllColour(colourSensor.getColourSensorReads());
        colourSensor.clear();
    }

    @Override
    public void visit(UsSensor usSensor) {
        serverRobotStateBuilder.setUltrasound(usSensor.getUltrasoundRead());
    }

    @Override
    public void visit(BatteryInfo batteryInfo) {
        serverRobotStateBuilder.setBattery(batteryInfo.getBatteryValues());
    }

    @Override
    public void visit(final ICamera camera) {
    }

    @Override
    public void visit(final IRobot robot) {
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String separator = " | ";
        Robot.ServerRobotState serverRobotState = getServerRobotState();
        for (int i = 0; i < serverRobotState.getRfidList().size(); i++) {
            stringBuilder.append(serverRobotState.getRfidList().get(i).getRfid());
            stringBuilder.append("; ");
        }
        stringBuilder.append(separator);
        for (int i = 0; i < serverRobotState.getColourList().size(); i++) {
            stringBuilder.append(serverRobotState.getColourList().get(i).getColour());
            stringBuilder.append("; ");
        }
        stringBuilder.append(separator);
        stringBuilder.append(serverRobotState.getUltrasound().getUltrasound());
        stringBuilder.append(separator);
        stringBuilder.append(serverRobotState.getBattery().getVoltageMilliVolt());
        return stringBuilder.toString();
    }

    protected void clearServerRobotState() {
        serverRobotStateBuilder.clear();
    }
}
