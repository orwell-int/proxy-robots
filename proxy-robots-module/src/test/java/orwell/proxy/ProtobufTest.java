package orwell.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.messages.Controller;
import orwell.messages.Robot;
import orwell.messages.ServerGame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link Controller.Input, @link Robot.ServerRobotState,
 * @link Robot.Register, @link ServerGame.GameState, @link ServerGame.Registered}.
 *
 * @author miludmann@gmail.com (Michael Ludmann)
 */

@RunWith(JUnit4.class)
public class ProtobufTest {
    public final static Logger logback = LoggerFactory.getLogger(ProtobufTest.class);
    private static final double INPUT_MOVE_LEFT = 50.5;
    private static final double INPUT_MOVE_RIGHT = 10.0;
    private static final boolean INPUT_FIRE_WEAPON_1 = false;
    private static final boolean INPUT_FIRE_WEAPON_2 = true;

    private static final long SERVER_ROBOT_STATE_TIMESTAMP = 1234567890;
    private static final String SERVER_ROBOT_STATE_RFID_VALUE = "11111111";
    private static final int SERVER_ROBOT_STATE_COLOUR_VALUE = 7;
    private static final String REGISTER_IMAGE = "imageTest";
    private static final String REGISTER_TEMPORARY_ROBOT_ID = "temporaryRobotId";
    private static final String REGISTER_VIDEO_URL = "http://video.url";
    private static final boolean GAME_STATE_PLAYING_VALUE = false;
    private static final long GAME_STATE_SECONDS = 180000;
    private static final String GAME_STATE_WINNER_STRING = "NicCage";
    private static final String REGISTERED_TEAM_NAME = "BLUE";
    private static final Robot.Status SERVER_ROBOT_STATE_RFID_STATUS = Robot.Status.ON;
    private static final Robot.Status SERVER_ROBOT_STATE_COLOUR_STATUS = Robot.Status.OFF;
    private static final String REGISTERED_ROBOT_ID = "robotIdTest";

    public static Controller.Input getTestInput() {
        final Controller.Input.Builder inputBuilder = Controller.Input.newBuilder();

        final Controller.Input.Fire.Builder fireBuilder = Controller.Input.Fire.newBuilder();
        fireBuilder.setWeapon1(INPUT_FIRE_WEAPON_1);
        fireBuilder.setWeapon2(INPUT_FIRE_WEAPON_2);
        inputBuilder.setFire(fireBuilder.build());


        final Controller.Input.Move.Builder moveBuilder = Controller.Input.Move.newBuilder();
        moveBuilder.setLeft(INPUT_MOVE_LEFT);
        moveBuilder.setRight(INPUT_MOVE_RIGHT);
        inputBuilder.setMove(moveBuilder.build());

        return inputBuilder.build();
    }

    public static boolean checkTestInputValid(final Controller.Input input) {
        assertTrue("Input should be initialized", input.isInitialized());

        assertTrue("Input contains Move data", input.hasMove());
        assertEquals(INPUT_MOVE_LEFT, input.getMove().getLeft(), 0);
        assertEquals(INPUT_MOVE_RIGHT, input.getMove().getRight(), 0);

        assertTrue("Input contains Fire data", input.hasFire());
        assertEquals(INPUT_FIRE_WEAPON_1, input.getFire().getWeapon1());
        assertEquals(INPUT_FIRE_WEAPON_2, input.getFire().getWeapon2());

        return true;
    }

    public static Robot.ServerRobotState getTestServerRobotState() {
        final Robot.ServerRobotState.Builder serverRobotStateBuilder = Robot.ServerRobotState.newBuilder();

        final Robot.Rfid.Builder rfidBuilder = Robot.Rfid.newBuilder();
        rfidBuilder.setRfid(SERVER_ROBOT_STATE_RFID_VALUE);
        rfidBuilder.setStatus(SERVER_ROBOT_STATE_RFID_STATUS);
        rfidBuilder.setTimestamp(SERVER_ROBOT_STATE_TIMESTAMP);
        serverRobotStateBuilder.addRfid(rfidBuilder.build());

        final Robot.Colour.Builder colourBuilder = Robot.Colour.newBuilder();
        colourBuilder.setColour(SERVER_ROBOT_STATE_COLOUR_VALUE);
        colourBuilder.setStatus(SERVER_ROBOT_STATE_COLOUR_STATUS);
        colourBuilder.setTimestamp(SERVER_ROBOT_STATE_TIMESTAMP);
        serverRobotStateBuilder.addColour(colourBuilder.build());

        return serverRobotStateBuilder.build();
    }

    public static boolean checkTestServerRobotState(final Robot.ServerRobotState serverRobotState) {
        assertTrue("ServerRobotState should be initialized", serverRobotState.isInitialized());

        assertEquals(SERVER_ROBOT_STATE_RFID_VALUE, serverRobotState.getRfid(0).getRfid());
        assertEquals(SERVER_ROBOT_STATE_RFID_STATUS, serverRobotState.getRfid(0).getStatus());
        assertEquals(SERVER_ROBOT_STATE_TIMESTAMP, serverRobotState.getRfid(0).getTimestamp());

        assertEquals(SERVER_ROBOT_STATE_COLOUR_VALUE, serverRobotState.getColour(0).getColour());
        assertEquals(SERVER_ROBOT_STATE_COLOUR_STATUS, serverRobotState.getColour(0).getStatus());
        assertEquals(SERVER_ROBOT_STATE_TIMESTAMP, serverRobotState.getColour(0).getTimestamp());

        return true;
    }

    public static Robot.Register getTestRegister() {
        final Robot.Register.Builder registerBuilder = Robot.Register.newBuilder();
        registerBuilder.setImage(REGISTER_IMAGE);
        registerBuilder.setTemporaryRobotId(REGISTER_TEMPORARY_ROBOT_ID);
        registerBuilder.setVideoUrl(REGISTER_VIDEO_URL);

        return registerBuilder.build();
    }

    public static boolean checkTestRegister(final Robot.Register register) {
        assertTrue("Register should be initialized", register.isInitialized());

        assertEquals(REGISTER_IMAGE, register.getImage());
        assertEquals(REGISTER_TEMPORARY_ROBOT_ID, register.getTemporaryRobotId());
        assertEquals(REGISTER_VIDEO_URL, register.getVideoUrl());

        return true;
    }

    public static ServerGame.GameState getTestGameState() {
        final ServerGame.GameState.Builder gameStateBuilder = ServerGame.GameState.newBuilder();
        gameStateBuilder.setPlaying(GAME_STATE_PLAYING_VALUE);
        gameStateBuilder.setSeconds(GAME_STATE_SECONDS);
        gameStateBuilder.setWinner(GAME_STATE_WINNER_STRING);

        return gameStateBuilder.build();
    }

    public static boolean checkTestGameState(final ServerGame.GameState gameState) {
        assertTrue("GameState should be initialized", gameState.isInitialized());

        assertEquals(GAME_STATE_PLAYING_VALUE, gameState.getPlaying());
        assertEquals(GAME_STATE_SECONDS, gameState.getSeconds());
        assertEquals(GAME_STATE_WINNER_STRING, gameState.getWinner());

        return true;
    }

    public static ServerGame.Registered getTestRegistered() {
        final ServerGame.Registered.Builder registeredBuilder = ServerGame.Registered.newBuilder();
        registeredBuilder.setRobotId(REGISTERED_ROBOT_ID);
        registeredBuilder.setTeam(REGISTERED_TEAM_NAME);

        return registeredBuilder.build();
    }

    public static boolean checkTestRegistered(final ServerGame.Registered registered) {
        assertTrue("Registered should be initialized", registered.isInitialized());

        assertEquals(REGISTERED_ROBOT_ID, registered.getRobotId());
        assertEquals(REGISTERED_TEAM_NAME, registered.getTeam());

        return true;
    }

    @Test
    public void testControllerInput() throws Exception {
        final Controller.Input input =
                Controller.Input.parseFrom(getTestInput().toByteArray());

        assertTrue("Controller.Input should contains all valid data",
                checkTestInputValid(input));
    }

    @Test
    public void testRobotServerRobotState() throws Exception {
        final Robot.ServerRobotState serverRobotState =
                Robot.ServerRobotState.parseFrom(getTestServerRobotState().toByteArray());

        assertTrue("Robot.ServerRobotState should contains all valid data",
                checkTestServerRobotState(serverRobotState));
    }

    @Test
    public void testRobotRegister() throws Exception {
        final Robot.Register register =
                Robot.Register.parseFrom(getTestRegister().toByteArray());

        assertTrue("Robot.Register should contains all valid data",
                checkTestRegister(register));
    }

    @Test
    public void testServerGameGameState() throws Exception {
        final ServerGame.GameState gameState =
                ServerGame.GameState.parseFrom(getTestGameState().toByteArray());

        assertTrue("ServerGame.GameState should contains all valid data",
                checkTestGameState(gameState));

    }

    @Test
    public void testServerGameRegistered() throws Exception {
        final ServerGame.Registered registered =
                ServerGame.Registered.parseFrom(getTestRegistered().toByteArray());

        assertTrue("ServerGame.Registered should contains all valid data",
                checkTestRegistered(registered));
    }
}
