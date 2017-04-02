package orwell.proxy.robot;

import orwell.messages.Robot;

public class RobotElementStateVisitor implements IRobotElementVisitor {
    private final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
    private boolean hasUpdate;

    protected Robot.ServerRobotState getServerRobotState() {
        return serverRobotStateBuilder.build();
    }

    /**
     * @return the byte array of ServerRobotState or
     * null if rfid and colour list are empty
     */
    public byte[] getServerRobotStateBytes() {
        final Robot.ServerRobotState serverRobotState = getServerRobotState();
        if (!hasRobotStateUpdate(serverRobotState)) {
            return null;
        } else {
            hasUpdate = false;
            return serverRobotState.toByteArray();
        }
    }

    private boolean hasRobotStateUpdate(Robot.ServerRobotState serverRobotState) {
        return serverRobotState != null && hasUpdate;
    }

    @Override
    public void visit(final ColourSensor colourSensor) {
        hasUpdate = hasUpdate || colourSensor.hasUpdate();
        serverRobotStateBuilder.addAllColour(colourSensor.getColourSensorReads());
        colourSensor.clear();
    }

    @Override
    public void visit(UsSensor usSensor) {
        hasUpdate = hasUpdate || usSensor.hasUpdate();
        serverRobotStateBuilder.setUltrasound(usSensor.getUltrasoundRead());
    }

    @Override
    public void visit(BatteryInfo batteryInfo) {
        hasUpdate = hasUpdate || batteryInfo.hasUpdate();
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
        for (int i = 0; i < serverRobotState.getColourList().size(); i++) {
            stringBuilder.append(serverRobotState.getColourList().get(i).getColour());
            stringBuilder.append("; ");
        }
        stringBuilder.append(separator);
        stringBuilder.append(serverRobotState.getUltrasound().getDistance());
        stringBuilder.append(separator);
        stringBuilder.append(serverRobotState.getBattery().getVoltageMillivolt());
        return stringBuilder.toString();
    }

    protected void clearServerRobotState() {
        serverRobotStateBuilder.clear();
    }
}
