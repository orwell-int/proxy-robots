package orwell.proxy.robot;

public interface IRobotElement {
    void accept(final IRobotElementVisitor visitor);
}
