package orwell.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by miludmann on 5/6/15.
 */
public class FrequencyFilter implements IFilter {
    final static Logger logback = LoggerFactory.getLogger(FrequencyFilter.class);
    private final long outGoingMsgFrequency;
    private HashMap<String, HashMap<EnumMessageType, Long>> timeRegistry =
            new HashMap<>();

    public FrequencyFilter(final long outGoingMsgFrequency) {
        this.outGoingMsgFrequency = outGoingMsgFrequency;
    }

    @Override
    public ZmqMessageBOM getfilteredMessage(ZmqMessageBOM inputMessage) {
        String routingId = inputMessage.getRoutingID();
        EnumMessageType enumMessageType = inputMessage.getMsgType();

        if(!timeRegistry.containsKey(routingId))
        {
            HashMap<EnumMessageType, Long> typeTimeEntry = new HashMap<>();
            typeTimeEntry.put(enumMessageType, System.currentTimeMillis());
            timeRegistry.put(routingId, typeTimeEntry);
        } else {
            final HashMap<EnumMessageType, Long> typeTimeEntry = timeRegistry.get(routingId);
            timeRegistry.get(routingId).put(enumMessageType, System.currentTimeMillis());
        }

        return inputMessage;
    }
}
