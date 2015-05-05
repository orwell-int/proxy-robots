package orwell.proxy;

import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;
import orwell.messages.Robot;
import orwell.messages.ServerGame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link orwell.proxy.ZmqMessageWrapper}.
 * <p/>
 * Created by parapampa on 15/03/15.
 */
public class ZmqMessageWrapperTest {

    final static Logger logback = LoggerFactory.getLogger(ZmqMessageWrapperTest.class);
    private String routingId = "NicCage";

    @TestSubject
    private ZmqMessageWrapper zmw;

    @Before
    public void setUp() {
        zmw = new ZmqMessageWrapper(getRawZmqMessage(EnumMessageType.REGISTERED));
    }

    private byte[] getRawZmqMessage(EnumMessageType messageType) {
        byte[] specificMessage = new byte[0];
        String zmqMessageHeader = null;

        switch (messageType) {
            case REGISTERED:
                specificMessage = getBytesRegistered();
                zmqMessageHeader = routingId + " " + "Registered" + " ";
                break;
            case SERVER_ROBOT_STATE:
                specificMessage = getBytesServerRobotState();
                zmqMessageHeader = routingId + " " + "ServerRobotState" + " ";
                break;
            case INPUT:
                specificMessage = getBytesInput();
                zmqMessageHeader = routingId + " " + "Input" + " ";
                break;
            case GAMESTATE:
                specificMessage = getBytesGameState();
                zmqMessageHeader = routingId + " " + "GameState" + " ";
                break;
            case UNKNOWN:
                specificMessage = new byte[1];
                zmqMessageHeader = routingId + " " + "bananaCage" + " ";
            default:
                logback.error("Case : Message type " + messageType + " not handled");
        }

        // Concatenate the two byte arrays
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(zmqMessageHeader.getBytes());
            outputStream.write(specificMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    public byte[] getBytesRegistered() {
        ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId("BananaOne");
        registeredBuilder.setTeam("BLUE");

        return registeredBuilder.build().toByteArray();
    }

    public byte[] getBytesInput() {
        Controller.Input.Builder inputBuilder = Controller.Input.newBuilder();
        Controller.Input.Fire.Builder fireBuilder = Controller.Input.Fire.newBuilder();
        Controller.Input.Move.Builder moveBuilder = Controller.Input.Move.newBuilder();
        fireBuilder.setWeapon1(false);
        fireBuilder.setWeapon2(false);
        moveBuilder.setLeft(0);
        moveBuilder.setRight(0);
        inputBuilder.setFire(fireBuilder.build());
        inputBuilder.setMove(moveBuilder.build());

        return inputBuilder.build().toByteArray();
    }

    public byte[] getBytesServerRobotState() {
        Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();
        Robot.Rfid.Builder rfidBuilder = Robot.Rfid.newBuilder();
        rfidBuilder.setRfid("1234");
        rfidBuilder.setStatus(Robot.Status.ON);
        rfidBuilder.setTimestamp(1234567890);
        serverRobotStateBuilder.addRfid(rfidBuilder.build());

        return serverRobotStateBuilder.build().toByteArray();
    }

    public byte[] getBytesGameState() {
        ServerGame.GameState.Builder gameStateBuilder = ServerGame.GameState.newBuilder();
        gameStateBuilder.setPlaying(true);

        return gameStateBuilder.build().toByteArray();
    }

    @Test
    public void testGetRoutingId() {
        assertEquals(routingId, zmw.getRoutingId());
    }

    @Test
    public void testGetMessageType() {
        zmw = new ZmqMessageWrapper(getRawZmqMessage(EnumMessageType.REGISTERED));
        assertEquals(EnumMessageType.REGISTERED, zmw.getMessageType());

        zmw = new ZmqMessageWrapper(getRawZmqMessage(EnumMessageType.INPUT));
        assertEquals(EnumMessageType.INPUT, zmw.getMessageType());

        zmw = new ZmqMessageWrapper(getRawZmqMessage(EnumMessageType.GAMESTATE));
        assertEquals(EnumMessageType.GAMESTATE, zmw.getMessageType());

        zmw = new ZmqMessageWrapper(getRawZmqMessage(EnumMessageType.SERVER_ROBOT_STATE));
        assertEquals(EnumMessageType.SERVER_ROBOT_STATE, zmw.getMessageType());
    }

    @Test
    public void testGetMessageTypeUnknown() {
        zmw = new ZmqMessageWrapper(getRawZmqMessage(EnumMessageType.UNKNOWN));
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

    @After
    public void tearDown() {
    }
}
