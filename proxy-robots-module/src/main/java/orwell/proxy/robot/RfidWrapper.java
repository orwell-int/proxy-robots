package orwell.proxy.robot;

import orwell.messages.Robot;

/**
 * Created by parapampa on 11/04/15.
 */
public class RfidWrapper implements ISensorWrapper {
    private final Robot.Rfid.Builder builder = Robot.Rfid.newBuilder();
    private String previousValue = null;

    public RfidWrapper() {
    }

    @Override
    public Robot.Rfid.Builder getBuilder() {
        return this.builder;
    }

    @Override
    public String getPreviousValue() {
        return this.previousValue;
    }

    @Override
    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }
}
