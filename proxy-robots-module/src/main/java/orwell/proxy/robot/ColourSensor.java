package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 5/12/15.
 */
public class ColourSensor implements IRobotElement {
    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }
}
