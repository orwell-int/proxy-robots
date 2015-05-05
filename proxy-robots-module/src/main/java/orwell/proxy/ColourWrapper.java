package orwell.proxy;

import orwell.messages.Robot;

/**
 * Created by parapampa on 11/04/15.
 */
public class ColourWrapper implements ISensorWrapper {
    private Robot.Colour.Builder builder = Robot.Colour.newBuilder();
    private int previousValue = -1;

    public ColourWrapper() {
    }

    @Override
    public Robot.Colour.Builder getBuilder() {
        return this.builder;
    }

    @Override
    public String getPreviousValue() {
        return Integer.toString(this.previousValue);
    }

    public void setPreviousValue(int previousValue) {
        this.previousValue = previousValue;
    }

    public int getPreviousValueInteger() {
        return this.previousValue;
    }

    @Override
    public void setPreviousValue(String previousValue) {
        this.previousValue = Integer.parseInt(previousValue);
    }
}
