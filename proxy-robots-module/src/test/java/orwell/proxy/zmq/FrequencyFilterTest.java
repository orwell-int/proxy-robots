package orwell.proxy.zmq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.EnumMessageType;

import static org.junit.Assert.*;

/**
 * Tests for {@link FrequencyFilter}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class FrequencyFilterTest {
    private final static Logger logback = LoggerFactory.getLogger(FrequencyFilterTest.class);
    private final static long OUTGOING_MSG_PERIOD_HIGH = 50000;
    private final static long OUTGOING_MSG_PERIOD_LOW = 0;
    private final static String TEST_ROUTING_ID_1 = "testRoutingId_1";
    private final static String TEST_ROUTING_ID_2 = "testRoutingId_2";
    private final static String TEST_MSG_BODY = "testMsgBody";
    private FrequencyFilter frequencyFilter;
    private ZmqMessageBOM zmqRegisterBOM_1;
    private ZmqMessageBOM zmqRegisterBOM_2;
    private ZmqMessageBOM zmqServerRobotStateBOM;


    @Before
    public void setUp() {
        zmqRegisterBOM_1 = getTestMessageBom(EnumMessageType.REGISTER, TEST_ROUTING_ID_1);
        zmqRegisterBOM_2 = getTestMessageBom(EnumMessageType.REGISTER, TEST_ROUTING_ID_2);
        zmqServerRobotStateBOM = getTestMessageBom(EnumMessageType.SERVER_ROBOT_STATE, TEST_ROUTING_ID_1);
    }

    private ZmqMessageBOM getTestMessageBom(final EnumMessageType messageType, final String routingId) {
        final byte[] msgBody = TEST_MSG_BODY.getBytes();
        return new ZmqMessageBOM(routingId, messageType, msgBody);
    }

    @Test
    public void testGetFilteredMessage_FirstGet() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        final ZmqMessageBOM filteredZmqMessage = frequencyFilter.getFilteredMessage(zmqRegisterBOM_1);
        assertNotNull("Filtered message should not be empty",
                filteredZmqMessage.getMessageBodyBytes());
        assertArrayEquals("Filtered message body should not be altered",
                zmqRegisterBOM_1.getMessageBodyBytes(),
                filteredZmqMessage.getMessageBodyBytes());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_SecondSameGetIsEmpty() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1));

        // The period of the filter is of a low tolerance,
        // so the second message should be empty as filtered
        assertTrue(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1).isEmpty());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_SecondSameGetIsNotEmpty() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_LOW);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1));
        Thread.sleep(1);
        // The period of the filter is of a high tolerance,
        // so the second message should not be filtered
        assertFalse(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1).isEmpty());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_SecondDifferentGetIsNotEmpty() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1));
        Thread.sleep(1);
        // The period of the filter is of a low tolerance,
        // but the message type is different
        // so the second message should not be filtered
        assertFalse(frequencyFilter.getFilteredMessage(zmqServerRobotStateBOM).isEmpty());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_DifferentRoutingIds() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1));
        Thread.sleep(1);
        // The period of the filter is of a high tolerance,
        // so the identical second message should be filtered
        assertTrue(frequencyFilter.getFilteredMessage(zmqRegisterBOM_1).isEmpty());

        Thread.sleep(1);
        // A third message with different routingId should NOT be filtered
        assertFalse(frequencyFilter.getFilteredMessage(zmqRegisterBOM_2).isEmpty());

        logback.debug("OUT");
    }

}
