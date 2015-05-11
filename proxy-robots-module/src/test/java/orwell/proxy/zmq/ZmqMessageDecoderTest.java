package orwell.proxy.zmq;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;
import orwell.messages.Robot;
import orwell.messages.ServerGame;
import orwell.proxy.EnumMessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link ZmqMessageDecoder}.
 * <p/>
 * Created by Michael Ludmann on 15/03/15.
 */
public class ZmqMessageDecoderTest {

    private final static Logger logback = LoggerFactory.getLogger(ZmqMessageDecoderTest.class);
    private static final String ROUTING_ID = "NicCage";
    private static final long TIMESTAMP = 1234567890;

    @TestSubject
    private ZmqMessageDecoder zmw;

    @Before
    public void setUp() {
        zmw = new ZmqMessageDecoder(getRawZmqMessage(EnumMessageType.REGISTERED));
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
        assertEquals(ROUTING_ID, zmw.getRoutingId());
    }

    @Test
    public void testGetMessageType() {
        zmw = new ZmqMessageDecoder(getRawZmqMessage(EnumMessageType.REGISTERED));
        assertEquals(EnumMessageType.REGISTERED, zmw.getMessageType());

        zmw = new ZmqMessageDecoder(getRawZmqMessage(EnumMessageType.INPUT));
        assertEquals(EnumMessageType.INPUT, zmw.getMessageType());

        zmw = new ZmqMessageDecoder(getRawZmqMessage(EnumMessageType.GAME_STATE));
        assertEquals(EnumMessageType.GAME_STATE, zmw.getMessageType());

        zmw = new ZmqMessageDecoder(getRawZmqMessage(EnumMessageType.SERVER_ROBOT_STATE));
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, zmw.getMessageType());
    }

    @Test
    public void testGetMessageTypeUnknown() {
        zmw = new ZmqMessageDecoder(getRawZmqMessage(EnumMessageType.UNKNOWN));
        assertEquals(EnumMessageType.UNKNOWN, zmw.getMessageType());
    }

    @Test
    public void testGetMessageBytes() {
        assertNotNull(zmw.getMessageBytes());
    }

    @Test
    public void testGetZmqMessageString() {
        assertEquals(new String(getRawZmqMessage(EnumMessageType.REGISTERED)), zmw.getZmqMessageString());
    }

    //TODO add test to test split function (in case there are spaces in the message body
}
