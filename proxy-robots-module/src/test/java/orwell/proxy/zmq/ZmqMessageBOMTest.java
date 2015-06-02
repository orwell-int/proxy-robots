package orwell.proxy.zmq;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;
import orwell.messages.Robot;
import orwell.messages.ServerGame;
import orwell.proxy.EnumMessageType;
import orwell.proxy.ProtobufTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ZmqMessageBOM}.
 * <p/>
 * Created by Michael Ludmann on 15/03/15.
 */
public class ZmqMessageBOMTest {

    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageBOMTest.class);
    private static final String ROUTING_ID = "NicCage";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @TestSubject
    private ZmqMessageBOM zmqMessageBom;

    @Before
    public void setUp() throws Exception {
    }

    private byte[] getRawZmqMessage(final EnumMessageType messageType) {
        byte[] specificMessage = new byte[0];
        String zmqMessageHeader = null;

        switch (messageType) {
            case REGISTERED:
                specificMessage = getBytesRegistered();
                zmqMessageHeader = ROUTING_ID + " " + "Registered" + " ";
                break;
            case SERVER_ROBOT_STATE:
                specificMessage = getServerRobotStateBytes();
                zmqMessageHeader = ROUTING_ID + " " + "ServerRobotState" + " ";
                break;
            case INPUT:
                specificMessage = getInputBytes();
                zmqMessageHeader = ROUTING_ID + " " + "Input" + " ";
                break;
            case GAME_STATE:
                specificMessage = getGameStateBytes();
                zmqMessageHeader = ROUTING_ID + " " + "GameState" + " ";
                break;
            case UNKNOWN:
                specificMessage = new byte[1];
                zmqMessageHeader = ROUTING_ID + " " + "bananaCage" + " ";
            default:
                logback.error("Case : Message type " + messageType + " not handled");
        }

        // Concatenate the two byte arrays
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            assert null != zmqMessageHeader;
            outputStream.write(zmqMessageHeader.getBytes());
            outputStream.write(specificMessage);
        } catch (final IOException e) {
            logback.error(e.getMessage());
        }

        return outputStream.toByteArray();
    }

    private byte[] getBytesRegistered() {
        return ProtobufTest.getTestRegistered().toByteArray();
    }

    private byte[] getInputBytes() {
        return ProtobufTest.getTestInput().toByteArray();
    }

    private byte[] getServerRobotStateBytes() {
        return ProtobufTest.getTestServerRobotState().toByteArray();
    }

    private byte[] getGameStateBytes() {
        return ProtobufTest.getTestGameState().toByteArray();
    }

    @Test
    public void testGetRoutingId() throws ParseException {
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.REGISTERED));
        assertEquals(ROUTING_ID, zmqMessageBom.getRoutingId());
    }

    @Test
    public void testGetMessageType() throws Exception {
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.REGISTERED));
        assertEquals(EnumMessageType.REGISTERED, zmqMessageBom.getMessageType());

        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.INPUT));
        assertEquals(EnumMessageType.INPUT, zmqMessageBom.getMessageType());

        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.GAME_STATE));
        assertEquals(EnumMessageType.GAME_STATE, zmqMessageBom.getMessageType());

        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.SERVER_ROBOT_STATE));
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, zmqMessageBom.getMessageType());
    }

    @Test
    public void testGetMessageTypeUnknown() throws Exception {
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.UNKNOWN));
        assertEquals(EnumMessageType.UNKNOWN, zmqMessageBom.getMessageType());
    }

    @Test
    public void testGetMessageBytes() throws ParseException {
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.REGISTERED));
        assertNotNull(zmqMessageBom.getMessageBodyBytes());
    }

    @Test
    public void testParseFrom_Exception() throws Exception {
        logback.info("IN");
        exception.expect(ParseException.class);
        zmqMessageBom = ZmqMessageBOM.parseFrom((ROUTING_ID + " Registered" + "").getBytes());
        logback.info("OUT");
    }

    /**
     * Test required to check integrity of decoded protobuf
     * @throws Exception
     */
    @Test
    public void testParseFrom_ParseProtobuf_Input() throws Exception {
        logback.info("IN");
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.INPUT));
        assertEquals(EnumMessageType.INPUT, zmqMessageBom.getMessageType());

        // Parse protobuf message
        final Controller.Input input =
                Controller.Input.parseFrom(zmqMessageBom.getMessageBodyBytes());
        assertTrue(ProtobufTest.checkTestInputValid(input));
        logback.info("OUT");
    }

    @Test
    public void testParseFrom_ParseProtobuf_Registered() throws Exception {
        logback.info("IN");
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.REGISTERED));
        assertEquals(EnumMessageType.REGISTERED, zmqMessageBom.getMessageType());

        // Parse protobuf message
        final ServerGame.Registered registered = ServerGame.Registered.parseFrom(zmqMessageBom.getMessageBodyBytes());
        assertTrue(ProtobufTest.checkTestRegistered(registered));

        logback.info("OUT");
    }

    @Test
    public void testParseFrom_ParseProtobuf_ServerRobotState() throws Exception {
        logback.info("IN");
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.SERVER_ROBOT_STATE));
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, zmqMessageBom.getMessageType());

        // Parse protobuf message
        final Robot.ServerRobotState serverRobotState =
                Robot.ServerRobotState.parseFrom(zmqMessageBom.getMessageBodyBytes());
        assertTrue(ProtobufTest.checkTestServerRobotState(serverRobotState));
        logback.info("OUT");
    }

}
