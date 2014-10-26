package orwell.common;

public interface MessageListenerInterface {
	void receivedNewMessage(UnitMessage msg); // Will be called when there is a
												// new message ready
}
