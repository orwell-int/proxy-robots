package orwell.proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Tests for {@link FrequencyFilter}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class FrequencyFilterTest {
    private final static Logger logback = LoggerFactory.getLogger(FrequencyFilterTest.class);
    private final long OUTGOING_MSG_PERIOD_HIGH = 50000;
    private final long OUTGOING_MSG_PERIOD_LOW = 0;
    private final String TEST_ROUTING_ID = "testRoutingId" ;
    private final String TEST_MSG_BODY = "testMsgBody" ;
    private FrequencyFilter frequencyFilter;
    private ZmqMessageBOM zmqRegisterBOM;
    private ZmqMessageBOM zmqServerRobotStateBOM;



    @Before
    public void setUp() throws Exception {
        zmqRegisterBOM = getTestMessageBom(EnumMessageType.REGISTER);
        zmqServerRobotStateBOM = getTestMessageBom(EnumMessageType.SERVER_ROBOT_STATE);
    }

    private ZmqMessageBOM getTestMessageBom(final EnumMessageType messageType) {
        final byte[] msgBody = new String(TEST_MSG_BODY).getBytes();
        return new ZmqMessageBOM(messageType, TEST_ROUTING_ID, msgBody);
    }

    @Test
    public void testGetFilteredMessage_FirstGet() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        final ZmqMessageBOM filteredZmqMessage = frequencyFilter.getFilteredMessage(zmqRegisterBOM);
        assertNotNull("Filtered message should not be empty",
                filteredZmqMessage.getMsgBodyBytes());
        assertArrayEquals("Filtered message body should not be altered",
                zmqRegisterBOM.getMsgBodyBytes(),
                filteredZmqMessage.getMsgBodyBytes());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_SecondSameGetIsEmpty() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM));

        // The period of the filter is of a low tolerance,
        // so the second message should be empty as filtered
        assertTrue(frequencyFilter.getFilteredMessage(zmqRegisterBOM).isEmpty());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_SecondSameGetIsNotEmpty() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_LOW);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM));
        Thread.sleep(1);
        // The period of the filter is of a high tolerance,
        // so the second message should not be filtered
        assertFalse(frequencyFilter.getFilteredMessage(zmqRegisterBOM).isEmpty());
        logback.debug("OUT");
    }

    @Test
    public void testGetFilteredMessage_SecondDifferentGetIsNotEmpty() throws Exception {
        logback.debug("IN");
        frequencyFilter = new FrequencyFilter(OUTGOING_MSG_PERIOD_HIGH);

        assertNotNull(frequencyFilter.getFilteredMessage(zmqRegisterBOM));
        Thread.sleep(1);
        // The period of the filter is of a low tolerance,
        // but the message type is different
        // so the second message should not be filtered
        assertFalse(frequencyFilter.getFilteredMessage(zmqServerRobotStateBOM).isEmpty());
        logback.debug("OUT");
    }


    // TODO new test with multiple routingIds

    @After
    public void tearDown() throws Exception {


    }
}
