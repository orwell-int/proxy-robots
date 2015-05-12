package orwell.proxy.robot;

import orwell.messages.Robot;

/**
 * Created by Michaël Ludmann on 11/04/15.
 */
public class ColourWrapper implements ISensorWrapper {
    public final static int INIT_VALUE = -1;
    private final Robot.Colour.Builder builder = Robot.Colour.newBuilder();
    private int previousValue = INIT_VALUE;

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

    @Override
    public void setPreviousValue(final String previousValue) {
        this.previousValue = Integer.parseInt(previousValue);
    }

    public int getPreviousValueInteger() {
        return this.previousValue;
    }
}
