package orwell.proxy.zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumMessageType;

import java.util.HashMap;

/**
 * Created by miludmann on 5/6/15.
 */
public class FrequencyFilter implements IFilter {
    final static Logger logback = LoggerFactory.getLogger(FrequencyFilter.class);
    private final long outGoingMsgFrequency;
    private final HashMap<String, HashMap<EnumMessageType, Long>> timeRegistry =
            new HashMap<>();

    public FrequencyFilter(final long outGoingMsgFrequency) {
        this.outGoingMsgFrequency = outGoingMsgFrequency;
    }

    @Override
    public ZmqMessageBOM getFilteredMessage(final ZmqMessageBOM inputMessage) {
        final String routingId = inputMessage.getRoutingId();
        final EnumMessageType enumMessageType = inputMessage.getMsgType();
        final long currentTime = System.currentTimeMillis();

        // If the routingId is new, we make a new entry, and return the inputMessage
        // (not filter)
        if (!timeRegistry.containsKey(routingId)) {
            final HashMap<EnumMessageType, Long> typeTimeEntry = new HashMap<>();
            typeTimeEntry.put(enumMessageType, currentTime);
            timeRegistry.put(routingId, typeTimeEntry);
        } else {
            // RoutingId exists. We check if it has already registered this message type.
            // if it is a new type for this id, we return the inputMessage (no filter)
            if (!timeRegistry.get(routingId).containsKey(enumMessageType)) {
                // Nothing to do
            } else {
                // MessageType already exists, we may have to filter the message.
                final long previousTime = timeRegistry.get(routingId).get(enumMessageType);
                // Check the time interval between previous message sent and current is
                // over the defined constant
                if (outGoingMsgFrequency < currentTime - previousTime) {
                    // Nothing to do, no filter
                } else {
                    // We filter the message
                    logback.debug("Message filtered: [" + routingId + "] + " + enumMessageType);
                    inputMessage.clearMsgBytes();
                }

            }
            // In both cases we register this event
            timeRegistry.get(routingId).put(enumMessageType, currentTime);
        }

        return inputMessage;
    }
}
