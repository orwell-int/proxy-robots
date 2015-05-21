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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link ZmqMessageBOM}.
 * <p/>
 * Created by Michael Ludmann on 15/03/15.
 */
public class ZmqMessageBOMTest {

    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageBOMTest.class);
    private static final String ROUTING_ID = "NicCage";
    private static final long TIMESTAMP = 1234567890;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @TestSubject
    private ZmqMessageBOM zmqMessageBom;

    @Before
    public void setUp() throws Exception {
        zmqMessageBom = ZmqMessageBOM.parseFrom(getRawZmqMessage(EnumMessageType.REGISTERED));
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
                specificMessage = getBytesServerRobotState();
                zmqMessageHeader = ROUTING_ID + " " + "ServerRobotState" + " ";
                break;
            case INPUT:
                specificMessage = getBytesInput();
                zmqMessageHeader = ROUTING_ID + " " + "Input" + " ";
                break;
            case GAME_STATE:
                specificMessage = getBytesGameState();
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
        final ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId("BananaOne");
        registeredBuilder.setTeam("BLUE");

        return registeredBuilder.build().toByteArray();
    }

    private byte[] getBytesInput() {
        final Controller.Input.Builder inputBuilder = Controller.Input.newBuilder();
        final Controller.Input.Fire.Builder fireBuilder = Controller.Input.Fire.newBuilder();
        final Controller.Input.Move.Builder moveBuilder = Controller.Input.Move.newBuilder();
        fireBuilder.setWeapon1(false);
        fireBuilder.setWeapon2(false);
        moveBuilder.setLeft(0);
        moveBuilder.setRight(0);
        inputBuilder.setFire(fireBuilder.build());
        inputBuilder.setMove(moveBuilder.build());

        return inputBuilder.build().toByteArray();
    }

    private byte[] getBytesServerRobotState() {
        final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
        final Robot.Rfid.Builder rfidBuilder = Robot.Rfid.newBuilder();
        rfidBuilder.setRfid("1234");
        rfidBuilder.setStatus(Robot.Status.ON);
        rfidBuilder.setTimestamp(TIMESTAMP);
        serverRobotStateBuilder.addRfid(rfidBuilder.build());

        return serverRobotStateBuilder.build().toByteArray();
    }

    private byte[] getBytesGameState() {
        final ServerGame.GameState.Builder gameStateBuilder = ServerGame.GameState.newBuilder();
        gameStateBuilder.setPlaying(true);

        return gameStateBuilder.build().toByteArray();
    }

    @Test
    public void testGetRoutingId() {
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
    public void testGetMessageBytes() {
        assertNotNull(zmqMessageBom.getMessageBodyBytes());
    }

    @Test
    public void testParseFrom_Exception() throws Exception {
        logback.info("IN");
        exception.expect(ParseException.class);
        zmqMessageBom = ZmqMessageBOM.parseFrom((ROUTING_ID + " Registered" + "").getBytes());
        logback.info("OUT");
    }

    //TODO add test to test split function (in case there are spaces in the message body
}
